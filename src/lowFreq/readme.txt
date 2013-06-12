Low frequency treatment:
1. Expand target terms with morphology prefixes
   Morphology4TargetTermExp.java
1. Collect candidates form the documents where he query appears (any ngram form bigram to four gram)
   GenerateNgrams.java
   Generates ngram file with frequencies (.lowfreq), including ngrams of frequency=1
2. Unit morphological variants of ngrams and update total counts
   GroupMorphVariants.java
   output: .comb file
3. Rank ngrams by FO similarity score (or tf*idf)