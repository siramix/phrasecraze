import sys

if len(sys.argv) != 5:
  print "Usage: %s <# phrases to generate> <pack Id> <starting ID> <name of outfile>" % sys.argv[0]
  print "   ex: generatePhraseFile.py 1000 2 1000 res/raw/pack2.json"
  sys.exit()

outfile = open(sys.argv[4], 'w')

for i in range(int(sys.argv[1])):
    phraseStr = '{"phrase": "WORD %d - PACK %d", "_id": %d, "difficulty": 1}\n' % (i+1, int(sys.argv[2]), int(sys.argv[3])+i+1)
    outfile.write(phraseStr)

outfile.close()
