#https://www.geeksforgeeks.org/removing-stop-words-nltk-python/
import os, sys
import re
import spacy

nlp = spacy.load('fr')    #python -m spacy download fr

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


#Set the input file here!
inputFile = "/home/junio/Desktop/Ju/SentenceClassification/dev/data-learningobjectives/SingleLabelLO-ascii.csv"
inputOutputPrefix = inputFile[0:-4]

# clean of broken lines and of a lot of characters
filteredFileNameTemp = inputOutputPrefix + '-LEMMATIZED-no-broken-lines-AND-cleaned.csv'
if os.path.exists(filteredFileNameTemp):
    os.remove(filteredFileNameTemp)
filteredFile = open(filteredFileNameTemp, 'a')

with open(inputFile) as fp:
    line1 = fp.readline()
    line2 = fp.readline()
    while line1:
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

        indexOfFirstComma = line1.index(',')
        lineQuestionId = line1[0:indexOfFirstComma + 1]
        if line1.endswith('\n'):
            line1 = line1[indexOfFirstComma + 1:-1]
        else:
            line1 = line1[indexOfFirstComma + 1:] #the last line of the file falls here, if there's not last \n
        indexOfLastComma = line1.rindex(',')
        #here, must chech if line ends with \n, or with \n\r to set the right index of the substring
        lineClass = line1[indexOfLastComma:-1]
        lineBody = line1[0:indexOfLastComma]

        doc = nlp(lineBody.decode('utf8'))
        lineBody = ''
        for word in doc:
            lineBody += word.lemma_ + ' '

        line1 = lineQuestionId + lineBody + lineClass
        line1 = line1.replace(' ,', ',')
        line1 = cleanWhiteSpaces(line1).encode("ascii", "ignore")

        filteredFile.write(line1+'\n')

        line1 = line2
        line2 = fp.readline()

filteredFile.close()