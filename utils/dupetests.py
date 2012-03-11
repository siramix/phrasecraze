import sys

if len(sys.argv) != 1:
    print "Usage: %s <logcat file>" % sys.argv[0]
    print "Make sure you stream the logcat output to this script (adb logcat | python dupetests.py)"
    sys.exit()

list = []
numlines = 0
for line in sys.stdin.readlines():
    if "Dealing" in line:
        # Add our word to the list of words encountered
        rhs = line.partition('::')[2]
        word = rhs.partition('::')[0]
        list.append(word)
        numlines += 1

counts = {}
packs = {}
wordsum = 0
for word in list:
    # Count each occurrance of each distinct word
    count = list.count(word)
    counts[word] = count
    wordsum += 1
    # Count each occurance of each distinct pack
    packId = word.partition('PACK ')[2]
    if packId in packs:
        packs[packId] += 1
    else:
        packs[packId] = 0

subcounts = {}
for word in counts:
    countkey = counts[word]
    print "%s : %s" % (word, counts[word])
    if countkey in subcounts:
        subcounts[countkey] += 1
    else:
        subcounts[countkey] = 1

print "TOTAL WORDS: %d" % wordsum
print "PACK COUNTS: ", packs
print "DISTRIBUTION OF WORD_COUNTS: ", subcounts
    
