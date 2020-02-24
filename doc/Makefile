all: navigation-life-cycle.png navigation-life-cycle.pdf

clean:
	rm -rf *.dot

%.dot:%.edn
	baubotanik $^ $@

%.png:%.dot
	dot -Tpng -o $@ $^

%.pdf:%.dot
	dot -Tpdf -o $@ $^

.PHONY: clean
