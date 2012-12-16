
DATE=`date +%s`
WEBSITE='http://news.ycombinator.com'

curl ${WEBSITE} | base64 | xargs redis-cli zadd hn-page ${DATE}
