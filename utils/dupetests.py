import sys

if len(sys.argv) != 1:
    print "Usage: %s <logcat file>" % sys.argv[0]
    print "Make sure you reference a logcat output (adb logcat -f outfile.txt)"
    sys.exit()



