import numpy as np
import cPickle
from collections import defaultdict
import sys, re
import pandas
import os

def build_data_for_cross_validation(listOfClassFiles, numberOfCrossValidationFolders=10, clean_string=True):
    """
    Loads data and split listOfCrossValidationFolders 10 folds.
    """
    sentences = []

    vocab = defaultdict(float)
    for i in range(0,len(listOfClassFiles)):
        with open(listOfClassFiles[i], "rb") as f:
            for line in f:
                qId = line[0:line.index(',')]
                line = line[line.index(',')+1:-1]
                sentence = []
                sentence.append(line.strip())   #strip removes all leading and trailing whitespaces are removed
                if clean_string:
                    orig_sentence = clean_str(" ".join(sentence))
                else:
                    orig_sentence = " ".join(sentence).lower()
                #build set of words found in each sentence - convert to set, excluding repetitions
                words = set(orig_sentence.split())
                #vocab: one entry per word, along with its appearance frequency
                for word in words:
                    vocab[word] += 1  #count frequency - since words is a set, it counts maximum once per sentence
                #json {y:class; text: original text; num_words: |sentence|; split: cross validation fold}
                datum  = {"y":i,
                          "text": orig_sentence,
                          "num_words": len(orig_sentence.split()),
                          "split": np.random.randint(0, numberOfCrossValidationFolders),
                          "qId": qId
                          }
                sentences.append(datum)
    return sentences, vocab
    
def convertWord2vecMapToMatrix(word2VectorMap, k=50):
    """
    Get word matrix. word2vecMatrix[i] is the vector for word indexed by i
    word_idx_map: map word->index; this index works both on word2vecMatrix indicating the vector of the i-th word
    """
    vocab_size = len(word2VectorMap)
    word_idx_map = dict()
    W = np.zeros(shape=(vocab_size+1, k), dtype='float32')            
    W[0] = np.zeros(k, dtype='float32')
    i = 1
    for word in word2VectorMap:
        W[i] = word2VectorMap[word]
        word_idx_map[word] = i
        i += 1
    return W, word_idx_map

def loadBinaryWord2VecFile(word2vecBinaryFileName, vocab):
    """
    Loads 200x1 word vecs from word2vec file indicated by word2vecBinaryFileName
    Returns vectors only for the words found in vocab
    """
    word2VectorMap = {}
    with open(word2vecBinaryFileName, "rb") as f:
        #read header
        header = f.readline()
        #the header of a word2vector holds the number of words and the vector size
        vocab_size, vectorSize = map(int, header.split())
        #size in bytes of each vector
        binaryVectorLenght = np.dtype('float32').itemsize * vectorSize

        #one word followed by its vector pe line - reads vocab_size words (the entire file)
        for line in xrange(vocab_size):
            word = []
            while True:
                ch = f.read(1)  #since the file is binary, it reads bytes one by one
                if ch == ' ':   #' ' is the marker character, it marks the start of the vector
                    word = ''.join(word)
                    break
                if ch != '\n':
                    word.append(ch)
            #only words found in vocab
            if word in vocab:
                # creates an array from a string, after reading binaryVectorLenght bytes
               word2VectorMap[word] = np.fromstring(f.read(binaryVectorLenght), dtype='float32')
            else: #pass by words not in vocab
                f.read(binaryVectorLenght)
    return word2VectorMap

def addRandomVectorsForWordsNotInTheWord2VecMap(word2VectorMap, vocab, minDocumentFrequency=1, k=50):
    """
    For words that occur in at least minDocumentFrequency documents (a document here is a sentence),
    creates a random word vector.    
    0.25 is chosen so the unknown vectors have (approximately) same variance as pre-trained ones
    """
    #checks every word in vocab; for words without a vector in word2VectorMap, creates a random vector
    for word in vocab:
        if word not in word2VectorMap and vocab[word] >= minDocumentFrequency:
            word2VectorMap[word] = np.random.uniform(-0.25,0.25,k)  

def countWordNotInTheWord2vec(word2VectorMap, vocab):
    missingWords = 0
    for word in vocab:
        if word not in word2VectorMap:
            missingWords += 1
    print "Number of words in the sentences that were not found in the word2vec file: " + str(missingWords)

def clean_str(string, TREC=False):
    """
    Tokenization/string cleaning for all datasets except for SST.
    Every dataset is lower cased except for TREC
    """
    string = re.sub(r"[^A-Za-z0-9(),!?\'\`]", " ", string)
    string = re.sub(r"\'s", " \'s", string)
    string = re.sub(r"\'ve", " \'ve", string)
    string = re.sub(r"n\'t", " n\'t", string)
    string = re.sub(r"\'re", " \'re", string)
    string = re.sub(r"\'d", " \'d", string)
    string = re.sub(r"\'ll", " \'ll", string)
    string = re.sub(r",", " , ", string)
    string = re.sub(r"!", " ! ", string)
    string = re.sub(r"\(", " \( ", string)
    string = re.sub(r"\)", " \) ", string)
    string = re.sub(r"\?", " \? ", string)
    string = re.sub(r"\s{2,}", " ", string)

    return string.strip() if TREC else string.strip().lower()

def clean_str_sst(string):
    """
    Tokenization/string cleaning for the SST dataset
    """
    string = re.sub(r"[^A-Za-z0-9(),!?\'\`]", " ", string)
    string = re.sub(r"\s{2,}", " ", string)
    return string.strip().lower()

if __name__=="__main__":
    #parameters here: k for vector size (used with word2vec software) and listOfCrossValidationFolders

    #---------------------------- Load of train and test data
    #one file for each class containing all the sentences of that class
    print "------------"
    listOfClassFiles = []
    for i in range(0,31):
        listOfClassFiles.append("OnlyQuestionsForNLP.tipo"+str(i))
    print 'Word2vec input file: ' + sys.argv[1]

    print "Loading train and test data from the " + str(len(listOfClassFiles)) + " files: OnlyQuestionsForNLP.tipoXX"
    # sentences: one entry per sentence as a json {y:class; text: original text; num_words: |sentence|; split: cross validation fold}
    # vocab: one entry per word, along with its appearance frequency
    sentences, vocab = build_data_for_cross_validation(listOfClassFiles, numberOfCrossValidationFolders=10, clean_string=True)
    #uses pandas framework to extract features from the corpus - here, the max number of words considering every sentence
    max_l = np.max(pandas.DataFrame(sentences)["num_words"])
    print "data loaded!"
    print "->synthesis of the " + str(len(listOfClassFiles)) + " files: OnlyQuestionsForNLP.tipoXX"
    print "-number of sentences: " + str(len(sentences))
    print "-number of words: " + str(len(vocab))
    print "-max sentence length: " + str(max_l)
    print "------------"
    #----------------------------Load word2vec data
    word2vecBinaryFileName = sys.argv[1]
    print "loading word2vec vectors from file " + word2vecBinaryFileName
    word2VectorMap = loadBinaryWord2VecFile(word2vecBinaryFileName, vocab)
    countWordNotInTheWord2vec(word2VectorMap, vocab)
    print "word2vec loaded!"
    print "number of words in word2vec file: " + word2vecBinaryFileName +": "+str(len(word2VectorMap))
    addRandomVectorsForWordsNotInTheWord2VecMap(word2VectorMap, vocab)
    word2vecMatrix, word_idx_map = convertWord2vecMapToMatrix(word2VectorMap)
    #----------------------------Generate random word2vec data
    randomWord2VectorMap = {}
    addRandomVectorsForWordsNotInTheWord2VecMap(randomWord2VectorMap, vocab) #since randomWord2VectorMap is empty, generates a vector for every word in vocab
    randomWord2vecMatrix, _ = convertWord2vecMapToMatrix(randomWord2VectorMap)
    # ----------------------------Save everyhing
    cPickle.dump([sentences, word2vecMatrix, randomWord2vecMatrix, word_idx_map, vocab], open(sys.argv[1] + str(max_l) + ".p", "wb"))
    print "dataset created in file:" + sys.argv[1] + str(max_l) + ".p"