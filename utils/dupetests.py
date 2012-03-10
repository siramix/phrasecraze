import sys

if len(sys.argv) != 1:
    print "Usage: %s <logcat file>" % sys.argv[0]
    print "Make sure you stream the logcat output to this script (adb logcat | python dupetests.py)"
    sys.exit()

list = []
numlines = 0
for line in sys.stdin.readlines():
    if "Dealing" in line:
        parts = line.partition('::')
        word = parts[2].partition('::')
        list.append(word[0])
        numlines += 1

counts = {}
sum = 0
for word in list:
    count = list.count(word)
    counts[word] = count
    sum += 1

subcounts = {}
for word in counts:
    countkey = counts[word]
    print "%s : %s" % (word, counts[word])
    if countkey in subcounts:
        subcounts[countkey] += 1
    else:
        subcounts[countkey] = 1
print "TOTAL: %d" % numlines
print "SUM: %d" % sum
print subcounts
    
