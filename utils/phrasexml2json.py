from xml.dom.minidom import parse
import sys
import codecs

if len(sys.argv) != 3:
  print "Usage: %s <xml input> <json output>" % sys.argv[0]
  sys.exit()
dom = parse(sys.argv[1])
f = codecs.open(sys.argv[2],'w','utf-8')
cards = dom.getElementsByTagName('phrase_entry')

"""
This is what we're up against. This is the enemy we face!
  <phrase_entry>
    <phrase>Harvard University</phrase>
    <difficulty>1</difficulty>
  </phrase_entry>
"""
for card in cards:
  phrase = card.getElementsByTagName('phrase')[0].childNodes[0].data
  difficulty = card.getElementsByTagName('difficulty')[0].childNodes[0].data
  f.write("{\"phrase\" : \"%s\", \"difficulty\" : %s}\n" % (phrase, difficulty))
f.close()
