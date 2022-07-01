#!/bin/sh
conffile=/var/lib/meetingsmanager/config.properties
dbfile=$(awk -F: '/^jdbcUrl=/ { print $3 }' $conffile )
domain=$(awk -F/ '/^baseUrl=/ { print $3 }' $conffile )
domainesc=$(echo $domain | sed -e s/\\./%2e/g)
tmpfile=$(mktemp)
sqlite3 $dbfile  'SELECT uid, password from teilnehmer WHERE active=1' >"$tmpfile"
cat "$tmpfile" | (
	IFS='|'
	while read uid password
	do

		if [ -f "/var/lib/prosody/$domainesc/accounts/$uid.dat" ]
		then
			continue
		fi

		prosodyctl register $uid $domain $password
	done
)

# remove inactive users
tmpfile2=$(mktemp)
(cd /var/lib/prosody/$domainesc/accounts; ls *.dat) >$tmpfile2
cat >$tmpfile2 <<EOF
meetmaster.dat
test1.dat
slederer.dat
EOF
for userfile in $(cat $tmpfile2)
do
	uid=$(basename $userfile .dat)
	if grep -q "^$uid|" $tmpfile
	then
		: prosody user exits in our user list, do not delete
	else
		prosodyctl deluser $uid@$domain
	fi
done
rm "$tmpfile2"
rm "$tmpfile"
