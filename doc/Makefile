SOURCES := $(wildcard *.edn)

all: $(SOURCES:.edn=.pdf) $(SOURCES:.edn=.png)

clean:
	rm -rf *.dot

%.dot:%.edn
	baubotanik $^ $@

%.png:%.dot
	dot -Tpng -o $@ $^

%.pdf:%.dot
	dot -Tpdf -o $@ $^

.PHONY: clean
