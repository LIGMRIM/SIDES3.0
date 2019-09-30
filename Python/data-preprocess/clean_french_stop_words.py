#https://www.geeksforgeeks.org/removing-stop-words-nltk-python/
import os, sys
#from nltk.corpus import stopwords
import nltk
from nltk.tokenize import word_tokenize
import re
import spacy

#nlp = spacy.load('fr')    #python -m spacy download fr
#nltk.download('stopwords')
#nltk.download('punkt')

def cleanhtml(raw_html):
  cleanr = re.compile('<.*?>')
  cleantext = re.sub(cleanr, '', raw_html)
  return cleantext

def cleanWhiteSpaces(word):
	word = word.replace('  ', ' ')
	word = word.replace('   ', ' ')
	word = word.replace('    ', ' ')
	word = word.replace('    ', ' ')
	word = word.replace('     ', ' ')
	word = word.replace('\t', ' ')
	return word

#word_tokenize accepts a string as an input, not a file.
#setTemp = set(stopwords.words('french'))
exclusion_words = set()
#for word in setTemp:
#	exclusion_words.add(word)

myOwnFrenchStopWords = open('french_stop_words.txt',"r")
for word in myOwnFrenchStopWords:
	exclusion_words.add(word[0:-1])
myOwnFrenchStopWords.close()

myOwnFrenchStopWords = open('french_stop_words-ASCII.txt',"r")
for word in myOwnFrenchStopWords:
	exclusion_words.add(word[0:-1])
myOwnFrenchStopWords.close()

frenchPronouns = open('french_pronouns-ASCII.txt',"r")
for word in frenchPronouns:
	exclusion_words.add(word[0:-1])
frenchPronouns.close()

first_clean = False

#Set the input file here!
inputFile = "/home/junio/Desktop/Ju/SentenceClassification/dev/data-learningobjectives/SingleLabelLO-ascii-LEMMATIZED-no-broken-lines-AND-cleaned.csv"
fileUsesDoubleQuotes = True
inputOutputPrefix = inputFile[0:-4]

if first_clean:
	# clean of broken lines and of a lot of characters
	filteredFileNameTemp = inputOutputPrefix + '-TEMP-no-broken-lines-AND-cleaned.csv'
	if os.path.exists(filteredFileNameTemp):
		os.remove(filteredFileNameTemp)
	filteredFile = open(filteredFileNameTemp, 'a')

	if fileUsesDoubleQuotes:
		with open(inputFile) as fp:
			line1 = fp.readline()
			line2 = fp.readline()
			while line1:
				loopSafeCounter = 0
				while not line2.startswith('"q'):
					line1 = line1[0:-2]
					line1 += ' '+line2
					line2 = fp.readline()
					if line2 == '': break
				line1 = line1.lower()
				line1 = cleanhtml(line1)
				line1 = line1.replace('","','";"')
				line1 = line1.replace(',', ' ')
				line1 = line1.replace('(s)', '')
				line1 = line1.replace('()', '')
				line1 = line1.replace('?', ' ')
				line1 = line1.replace('";"',',')
				line1 = line1.replace('"', '')
				line1 = line1.replace(':', '')
				line1 = line1.replace('qu\'', '')
				line1 = line1.replace('c\'', '')
				line1 = line1.replace('d\'', '')
				line1 = line1.replace('l\'', '')
				line1 = line1.replace('s\'', '')
				line1 = line1.replace('m\'', '')
				line1 = line1.replace('n\'', '')
				line1 = line1.replace('v\'', '')
				line1 = line1.replace('o\'', '')
				line1 = line1.replace('t\'', '')
				line1 = line1.replace('f\'', '')
				line1 = line1.replace('j\'', '')
				line1 = line1.replace('e\'e', ' ')
				line1 = line1.replace('-vous', '')
				line1 = line1.replace('-t-on', '')
				line1 = line1.replace('-on', '')
				line1 = line1.replace('-ci', '')
				line1 = line1.replace('-elle', '')
				line1 = line1.replace('-elles', '')
				line1 = line1.replace('-il', '')
				line1 = line1.replace('-ils', '')
				line1 = line1.replace('-ne', '')
				line1 = line1.replace('- ', '')
				line1 = line1.replace(' -', '')
				line1 = line1.replace('\'.', ' ')
				line1 = line1.replace('\'', ' ')
				line1 = line1.replace('.', '. ')
				line1 = cleanWhiteSpaces(line1)
				filteredFile.write(line1)

				line1 = line2
				line2 = fp.readline()
	else:
		with open(inputFile) as fp:
			line1 = fp.readline()
			line2 = fp.readline()
			while line1:
				while not line2.startswith('q'):
					line1 = line1[0:-2]
					line1 += ' ' + line2
					line2 = fp.readline()
					if line2 == '': break
				line1 = cleanhtml(line1)
				#line1 = line1.replace(',', ' ')
				line1 = line1.replace('(s)', '')
				line1 = line1.replace('()', '')
				line1 = line1.replace('?', ' ')
				line1 = line1.replace('"', '')
				line1 = line1.replace(':', '')
				line1 = cleanWhiteSpaces(line1)
				filteredFile.write(line1)

				line1 = line2
				line2 = fp.readline()
		filteredFile.close()
else:
	filteredFileNameTemp = inputFile

filteredFileName = inputOutputPrefix + '-FINAL-no-stop-words.csv'
if os.path.exists(filteredFileName):
	os.remove(filteredFileName)
filteredFile = open(filteredFileName, 'a')
wordFrequency = {}
lemmaFrequency = {}
with open(filteredFileNameTemp) as fp:
	line = fp.readline()
	indexOfFirstComma = 0
	while line:
		indexOfFirstComma = line.index(',')
		lineQuestionId = line[0:indexOfFirstComma+1]
		line = line[indexOfFirstComma+1:-1]

		indexOfLastComma = line.rindex(',')
		# here, must chech if line ends with \n, or with \n\r to set the right index of the substring
		lineClass = line[indexOfLastComma:]
		lineBody = line[0:indexOfLastComma]

		lineBody = lineBody.replace('(','')
		lineBody = lineBody.replace(')','')
		lineBody = lineBody.replace('/','')
		lineBody = lineBody.replace('\\','')
		lineBody = lineBody.replace('0','')
		lineBody = lineBody.replace('1','')
		lineBody = lineBody.replace('2','')
		lineBody = lineBody.replace('3','')
		lineBody = lineBody.replace('4','')
		lineBody = lineBody.replace('5','')
		lineBody = lineBody.replace('6','')
		lineBody = lineBody.replace('7','')
		lineBody = lineBody.replace('8','')
		lineBody = lineBody.replace('9','')
		lineBody = lineBody.replace('  ',' ')

		words = word_tokenize(lineBody)
		lineBody = ''
		for word in words:
			if (len(word) > 3 and not word in exclusion_words):
				if word in wordFrequency:
					wordFrequency[word] += 1
				else:
					wordFrequency[word] = 1
				lineBody += word +' '

		#doc = nlp(lineBody.decode('utf8'))
		#lineBody = ''
		#for word in doc:
		#	if word.lemma_ in lemmaFrequency:
		#		lemmaFrequency[word.lemma_] += 1
		#	else:
		#		lemmaFrequency[word.lemma_] = 1
		#	lineBody += word.lemma_ + ' '

		line = lineQuestionId+lineBody+lineClass
		line = line.replace(' ,',',')
		line = cleanWhiteSpaces(line).encode("ascii","ignore")

		filteredFile.write(line + '\n')
		line = fp.readline()
filteredFile.close()


wordFrequency = sorted(wordFrequency.items(), key=lambda x: x[1], reverse=True)
totalCount = 0
for CODE, value in wordFrequency:
	totalCount += value

accumulated = 0
file = open(inputOutputPrefix + '-ANALYSIS-wordFrequency.csv', 'w')
file.write('word;frequency;percentage;accumulated' + '\n')
for word, value in wordFrequency:
	percentage = value / float(totalCount)
	accumulated += percentage
	file.write(str(word) + '; ' + str(value))
	file.write(";" + "%.2f" % percentage)
	file.write(";" + "%.2f" % accumulated + '\n')
file.close()



lemmaFrequency = sorted(lemmaFrequency.items(), key=lambda x: x[1], reverse=True)
totalCount = 0
for CODE, value in lemmaFrequency:
	totalCount += value

accumulated = 0
file = open(inputOutputPrefix + '-ANALYSIS-wordFrequency-LEMMA.csv', 'w')
file.write('word(lemma);frequency;percentage;accumulated' + '\n')
for word, value in lemmaFrequency:
	percentage = value / float(totalCount)
	accumulated += percentage
	file.write(str(word) + '; ' + str(value))
	file.write(";" + "%.2f" % percentage)
	file.write(";" + "%.2f" % accumulated + '\n')
file.close()
