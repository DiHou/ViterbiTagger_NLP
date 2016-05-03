Instructions

The minimum requirement for this assignment is to implement and train a part-of-speech tagger using the Viterbi algorithm, as described in class and in section 5.5 of the text (second ed.).

We provide you with a copy of the Wall Street Journal corpus, which is the standard corpus used for testing POS taggers. This corpus is divided into 25 segments, numbered 00 to 24, each with roughly 40,000 words. Conventionally segments 00, 01 and 22 are not used for POS tagging experiments; segments 02 through 21 are used for training; segment 24 is used as the development corpus; and segment 23 is used as the test corpus. This data is made available to you as a resource through NYU Classes in the form of a zip file, WSJ_POS_CORPUS_FOR_STUDENTS.zip. This zip file contains

WSJ_02-21.pos: words and tags for training corpus
WSJ_24.word: words for development corpus
WSJ_24.pos: words and tags for development corpus
WSJ_23.word: words for test corpus
Files with a word extension have the document text, one word per line; for those with a pos extension, each line consists of a word, a tab character, and a part-of-speech. In both formats a sentence boundary is indicated by a empty line.
You should train your tagger on the training corpus and evaluate its performance on segment 24, the development corpus. We provide a simple Python scoring program and an equivalent Java version for this purpose. Having a development corpus allows you to evaluate alternatives in designing the tagger -- for example, how to treat unknown words. When you are satisfied with your tagger you should run it on the test data and submit the result. You have the option of training on the union of the training and development corpora before making this final run. Note that we do not provide the key (pos file) for segment 23; you should not train or do development using the test data.