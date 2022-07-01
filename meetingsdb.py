#!/usr/bin/python
# encoding: utf-8
# vim: tabstop=4 expandtab shiftwidth=4 softtabstop=4

import sys
import sqlite3
import icalendar
import pytz
import datetime
import smtplib
from os.path import basename
from email.mime.application import MIMEApplication
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.utils import COMMASPACE, formatdate
from string import Template


tz = pytz.timezone('Europe/Berlin')
#tz = pytz.UTC

organisator_template = u"""
Sie sind der Organisator einer Videokonferenz am $datum.\n
Zum Beginn der Konferenz folgen Sie bitte diesem Link:
  $url

Ihre Jitsi-Benutzerkennung: $organisator_uid
Ihr Passwort: $organisator_password

Die Benutzerkennung ist nur an diesem Datum gültig.
"""

teilnehmer_template = u"""
Sie wurden von $organisator_email zu einer Videokonferenz am $datum eingeladen.\n
Zum Beitreten der Konferenz folgen Sie bitte diesem Link:
  $url
"""

email_sender = "CHANGE@THIS.de"

def send_mail(send_from, send_to, subject, text, icaldata,
                  server="127.0.0.1"):
    attachment_name = 'Konferenz.ics'

    msg = MIMEMultipart()
    msg['From'] = send_from
    msg['To'] = "<%s>" % send_to
    msg['Date'] = formatdate(localtime=True)
    msg['Subject'] = subject

    msg.attach(MIMEText(text, "plain", "utf-8"))

    part = MIMEApplication(icaldata, Name=basename(attachment_name))
    part['Content-Disposition'] = 'attachment; filename="%s"' % basename(attachment_name)
    msg.attach(part)

    # print msg.as_string()
    print send_from, send_to

    smtp = smtplib.SMTP(server)
    smtp.sendmail(send_from, [ send_to ], msg.as_string())
    smtp.close()


def open_db(path):
    db = sqlite3.connect(path)
    return db


def close_db(db):
    db.close()


def set_meeting_to_notified(db, meeting_id):
    c = db.cursor()
    c.execute("UPDATE meetings SET notified=1 WHERE id=?", (meeting_id,))
    db.commit()


def remove_old_meetings(db):
    c = db.cursor()
    c.execute("SELECT * FROM meetings WHERE date(beginn/1000,'unixepoch','localtime') < date('now')")
    for r in c.fetchall():
        # print r
        meeting_id=r[0]
        c.execute("DELETE FROM meetings WHERE id=?", (meeting_id,))
        c.execute("DELETE FROM teilnehmer WHERE external=1 AND id IN (SELECT teilnehmer_id FROM meeting_teilnehmer WHERE meeting_id=?)", (meeting_id,))
        c.execute("UPDATE teilnehmer SET active=0 WHERE external=0 AND id IN (SELECT organisator_id FROM meetings WHERE id=?)", (meeting_id,))
        c.execute("DELETE FROM meeting_teilnehmer WHERE meeting_id=?", (meeting_id,))
       
        db.commit()


def remove_old_external_teilnehmer(db):
    c = db.cursor()
    c.execute("DELETE FROM teilnehmer WHERE external=1 AND id NOT IN (SELECT teilnehmer_id FROM meeting_teilnehmer)")
    db.commit()


def deactivate_old_organisator(db):
    c = db.cursor()
    c.execute("UPDATE teilnehmer SET active=0 WHERE active=1 AND permanent=0 AND id NOT IN (SELECT organisator_id FROM meetings)")
    db.commit()


def process_unsent_meetings(db):
    c = db.cursor()
    c.execute("SELECT id, name, beginn, ende, organisator_id, resource, url, password, notified FROM meetings WHERE notified=0")
    for r in c.fetchall():
        meeting_id, name, beginn, ende, organisator_id, resource, url, password, notified = r
        datum = datetime.date.fromtimestamp(beginn / 1000).strftime("%d.%m.%Y")
        dt_beginn = datetime.datetime.fromtimestamp(beginn / 1000, pytz.UTC)
        dt_ende = datetime.datetime.fromtimestamp(ende / 1000, pytz.UTC)
        organisator = get_teilnehmer_by_id(db, organisator_id)
        # print r, name, dt_beginn, dt_ende, organisator

        teilnehmer = get_meeting_teilnehmer(db, meeting_id)
     
        organisator_uid = organisator[0]
        organisator_email = organisator[1]
        organisator_password = organisator[4]

        description = "Sie wurden von %s zu einer Videokonferenz am %s eingeladen.\n" \
            "Um der Konferenz beizutreten, folgen Sie bitte diesem Link:\n %s" % \
            (organisator_email, datum, url)

        cal = icalendar.Calendar()
        cal.add('prodid','-//MeetingsManager//dass-it.de//')
        cal.add('version','2.0')

        event = icalendar.Event()
        event.add('summary', 'Videokonferenz ' + name)
        event.add('dtstart', dt_beginn)
        event.add('dtend', dt_ende)
        event.add('dtstamp', datetime.datetime.now())
        event.add('priority', 5)
        # event.add('location', url)
        event.add('description', description)
        event.add('organizer', 'MAILTO:' + organisator[1])

        
        if teilnehmer is not None:
            for t in teilnehmer:
                event.add('attendee', icalendar.vCalAddress('MAILTO:' + t[1]))

        cal.add_component(event)

        ical_data = cal.to_ical()

        if teilnehmer is not None:
            for t in teilnehmer:
                s = Template(teilnehmer_template)
                s = s.substitute(organisator_email=organisator_email, url=url, datum=datum, organisator_password=organisator_password)
                send_mail(email_sender,t[1],"Einladung zu einer Videokonferenz", s, ical_data)

        s = Template(organisator_template)
        s = s.substitute(organisator_uid=organisator_uid, organisator_email=organisator_email, url=url, datum=datum, \
                organisator_password=organisator_password, meeting_password=password)
        send_mail(email_sender,organisator_email,"Organisator einer Videokonferenz", s, ical_data)
        set_meeting_to_notified(db, meeting_id)


def get_todays_meetings(db):
    c = db.cursor()
    c.execute("SELECT id, name, beginn, ende, organisator_id, resource, url, password, notified FROM meetings WHERE date(beginn/1000,'unixepoch','localtime') = date('now')")
    for r in c.fetchall():
        meeting_id, name, beginn, ende, organisator_id, resource, url, password, notified = r
        dt_beginn = datetime.datetime.fromtimestamp(beginn / 1000, tz)
        dt_ende = datetime.datetime.fromtimestamp(ende / 1000, tz)
        organisator = get_teilnehmer_by_id(db, organisator_id)
        # print r, name, dt_beginn, dt_ende, organisator

        get_meeting_teilnehmer(db, meeting_id)


def get_teilnehmer_by_id(db, t_id):
    c = db.cursor()
    c.execute("SELECT uid, email, permanent, external, password FROM teilnehmer WHERE id=?", (t_id,))
    uid, email, permanent, external, password = c.fetchone()
    return (uid, email, permanent, external, password)
    

def get_meeting_teilnehmer(db, meeting_id):
    #print "get_meeting_teilnehmer", meeting_id
    c = db.cursor()
    c.execute("SELECT uid, email, permanent, external FROM teilnehmer WHERE id IN (SELECT teilnehmer_id FROM meeting_teilnehmer WHERE meeting_id=?)", (meeting_id,))
    result = c.fetchall()
    #for row in result:
    #   print row
    return result

def get_all_meetings(db):
    c = db.cursor()
    c.execute("SELECT id, name, beginn, ende, organisator_id, resource, url, password, notified FROM meetings")
    for r in c.fetchall():
        meeting_id, name, beginn, ende, organisator_id, resource, url, password, notified = r
        dt_beginn = datetime.datetime.fromtimestamp(beginn / 1000, tz)
        dt_ende = datetime.datetime.fromtimestamp(ende / 1000, tz)
        print r, name, dt_beginn, dt_ende, get_teilnehmer_by_id(db, organisator_id)


if __name__ == '__main__':
    dbpath = sys.argv[1]
    cmd = sys.argv[2]

    db = open_db(dbpath)

    if cmd == 'mail':
        process_unsent_meetings(db)
    elif cmd == 'clean':
        remove_old_meetings(db)
        remove_old_external_teilnehmer(db)
        deactivate_old_organisator(db)

    close_db(db)
