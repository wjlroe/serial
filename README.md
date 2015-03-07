# serial

A Clojure library designed to ... well, that part is up to you.

## Usage

FIXME

```
mkdir -p ~/.overtone/orchestra/cello && cd ~/.overtone/orchestra && cd
~/.overtone/orchestra && wget --quiet --no-clobber
http://www.philharmonia.co.uk/assets/audio/samples/cello/cello.zip &&
unzip -n -d cello cello.zip && cd cello && cd ~/.overtone/orchestra &&
wget --quiet --no-clobber
http://www.philharmonia.co.uk/assets/audio/samples/oboe/oboe.zip &&
unzip -n -d oboe oboe.zip && cd oboe && cd ~/.overtone/orchestra &&
wget --quiet --no-clobber
http://www.philharmonia.co.uk/assets/audio/samples/double%20bass/double%20bass.zip
&& unzip -n -d "double-bass" "double bass.zip" && cd "double-bass" &&
cd ~/.overtone/orchestra && wget --quiet --no-clobber
http://www.philharmonia.co.uk/assets/audio/samples/percussion/percussion.zip
&& unzip -n -d percussion percussion.zip && cd percussion && cd
~/.overtone/orchestra && wget --quiet --no-clobber
http://www.philharmonia.co.uk/assets/audio/samples/violin/violin.zip
&& unzip -n -d violin violin.zip && cd violin && cd
~/.overtone/orchestra && for f in cd ~/.overtone/orchestra/**/*.mp3;
do ffmpeg -loglevel quiet -y -i "$f" "${f%.mp3}.wav"; done && find
~/.overtone/orchestra -name "*.mp3" -delete
```

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
