FOP=/tmp/fop-2.5/fop
CP=$FOP/build/fop.jar:$FOP/lib/commons-logging-1.0.4.jar:$FOP/lib/commons-io-1.3.1.jar:$FOP/lib/xmlgraphics-commons-2.4.jar:$FOP/lib/fontbox-2.0.16.jar:$FOP/lib/batik-all-1.13.jar


for file in *.ttf; do
  java -cp $CP org.apache.fop.fonts.apps.TTFReader ${file%.*}.ttf ${file%.*}.xml
done
