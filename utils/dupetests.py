import sys

if len(sys.argv) != 2:
    print "Usage: %s <logcat file>" % sys.argv[0]
    print "Make sure you reference a logcat output (adb logcat -d > outfile.txt)"
    sys.exit()

f = open(sys.argv[1], 'r')

list = []

for line in f.readlines():
    if " Delt " in line:
        words = line.split()
        list.append(words[4])

for word in list:
    print word, ":", list.count(word)

