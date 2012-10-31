
.PHONY:

rmtilde:
	find -name "*~" | xargs rm

clean:
	find -name "*.class" | xargs rm
	rm -f test_packet.jvs*
	rm -f doc/*.html

wc:
	find -name "*.java" | xargs wc
