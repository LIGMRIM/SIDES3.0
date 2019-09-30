"""
Customized code from the original source at: https://github.com/yoonkim/CNN_sentence

Concerning the following paper:
Convolutional Neural Networks for Sentence Classification
http://arxiv.org/pdf/1408.5882v2.pdf

Much of the code is modified from
- deeplearning.net (for ConvNet classes)
- https://github.com/mdenil/dropout (for dropout)
- https://groups.google.com/forum/#!topic/pylearn-dev/3QbKtCumAW4 (for Adadelta)
"""

import cPickle
import numpy
from collections import OrderedDict
import theano
import theano.tensor as T
import warnings
import sys
import time
import os
import pandas as pd

warnings.filterwarnings("ignore")
global workingDirectory

#different non-linearities
def ReLU(x):
	y = T.maximum(0.0, x)
	return(y)
def Sigmoid(x):
	y = T.nnet.sigmoid(x)
	return(y)
def Tanh(x):
	y = T.tanh(x)
	return(y)
def Iden(x):
	y = x
	return(y)

def getLabelFromTypeId(typeId):
	if typeId == 0:
		return 'addictology'
	elif typeId == 1:
		return 'anesthesiology_resuscitation_emergencies'
	elif typeId == 2:
		return 'cancerology_radiotherapy'
	elif typeId == 3:
		return 'cardiovascular'
	elif typeId == 4:
		return 'dermatology'
	elif typeId == 5:
		return 'digestive_surgery'
	elif typeId == 6:
		return 'endocrinology_metabolism_nutrition'
	elif typeId == 7:
		return 'forensic_medecine'
	elif typeId == 8:
		return 'gerontology'
	elif typeId == 9:
		return 'hematology'
	elif typeId == 10:
		return 'hepatogastroenterology'
	elif typeId == 11:
		return 'infectious_diseases'
	elif typeId == 12:
		return 'internal_medicine'
	elif typeId == 13:
		return 'maxillofacial_surgery'
	elif typeId == 14:
		return 'nephrology'
	elif typeId == 15:
		return 'neurology'
	elif typeId == 16:
		return 'neurosurgery'
	elif typeId == 17:
		return 'obstetric_gynecology'
	elif typeId == 18:
		return 'occupational_medicine'
	elif typeId == 19:
		return 'ophthalmology'
	elif typeId == 20:
		return 'orthopedics'
	elif typeId == 21:
		return 'otorhinolaryngology'
	elif typeId == 22:
		return 'pediatrics'
	elif typeId == 23:
		return 'physical_medicine_and_rehabilitation'
	elif typeId == 24:
		return 'pneumonology'
	elif typeId == 25:
		return 'psychiatry'
	elif typeId == 26:
		return 'public_health'
	elif typeId == 27:
		return 'rheumatology'
	elif typeId == 28:
		return 'therapeutic_pharmacology'
	elif typeId == 29:
		return 'toxicology'
	elif typeId == 30:
		return 'urology'




def train_conv_net(trainAndTestData_array,
				   U,
				   n_epochs,
				   img_w=100,
				   filter_hs=[3,4,5],
				   hidden_units=[100,31],
				   dropout_rate=[0.5],
				   batch_size=50,
				   learningDecay = 0.95,
				   activationFunction="relu",
				   activations=[Iden],
				   sqr_norm_lim=9,
				   nonStatic=True):
	rng = numpy.random.RandomState(3435)
	#retrieve length of prepared data size by inspecting the first element of the first array
	#this length is calculated in function prepareTrainAndTestForIthCrossValidation
	dataLenght_int = len(trainAndTestData_array[0][0])-1

	#---------------------------------filters stuff
	filter_w = img_w
	filter_shapes = []
	pool_sizes = []
	feature_maps = hidden_units[0]
	#defines filters and pooling
	for filter_h in filter_hs:
		filter_shapes.append((feature_maps, 1, filter_h, filter_w))
		pool_sizes.append((dataLenght_int-filter_h+1, img_w-filter_w+1))
	#--------------------------------data preparation
	#if dataset size is not a multiple of mini batches, replicates extra data (at random)
	#trainAndTestData_array[0] -> train data;  trainAndTestData_array[1] -> test data
	if trainAndTestData_array[0].shape[0] % batch_size > 0:
		extra_data_num = batch_size - trainAndTestData_array[0].shape[0] % batch_size
		train_set = numpy.random.permutation(trainAndTestData_array[0])
		extra_data = train_set[:extra_data_num]
		trainDataBatch=numpy.append(trainAndTestData_array[0],extra_data,axis=0)
	else:
		trainDataBatch = trainAndTestData_array[0]

	trainDataBatch = numpy.random.permutation(trainDataBatch)
	n_batches = trainDataBatch.shape[0]/batch_size
	n_train_batches = int(numpy.round(n_batches*0.9))
	n_val_batches = n_batches - n_train_batches

	#train and validation trainAndTestData_array
	train_set = trainDataBatch[:n_train_batches*batch_size,:]
	train_set_x, train_set_y = train_set[:,:dataLenght_int],train_set[:,-1]
	val_set = trainDataBatch[n_train_batches*batch_size:,:]
	val_set_x, val_set_y = val_set[:,:dataLenght_int],val_set[:,-1]
	#test data
	test_set_x = trainAndTestData_array[1][:,:dataLenght_int]
	test_set_y = numpy.asarray(trainAndTestData_array[1][:,-1],"int32")
	test_set_qIds =  trainAndTestData_array[3]
	test_pred_layers = []
	test_size = test_set_x.shape[0]

	#---------------------------main model definition: COST
	index = T.lscalar()
	x = T.matrix('x')
	y = T.ivector('y')   #  => y is the vector of correct answers, one class per sample in a batch
	#copy matrix of word2vectors to the GPU memory (if available)
	wordsRepresentations = theano.shared(value = U, name = "word2vectorMatrix")

	zero_vec_tensor = T.vector()
	zero_vec = numpy.zeros(img_w)
	set_zero = theano.function([zero_vec_tensor], updates=[(wordsRepresentations, T.set_subtensor(wordsRepresentations[0,:], zero_vec_tensor))], allow_input_downcast=True)

	#prepare input to the network
	layer0_input = wordsRepresentations[T.cast(x.flatten(),dtype="int32")].reshape((x.shape[0],1,x.shape[1],wordsRepresentations.shape[1]))
	conv_layers = []
	layer1_inputs = []

	for i in xrange(len(filter_hs)):
		filter_shape = filter_shapes[i]
		pool_size = pool_sizes[i]
		conv_layer = LeNetConvPoolLayer(rng, input=layer0_input,image_shape=(batch_size, 1, dataLenght_int, img_w),
								filter_shape=filter_shape, poolsize=pool_size, non_linear=activationFunction)
		layer1_input = conv_layer.output.flatten(2)
		conv_layers.append(conv_layer)
		layer1_inputs.append(layer1_input)

	#input to second layer
	layer1_input = T.concatenate(layer1_inputs,1)
	hidden_units[0] = feature_maps*len(filter_hs)

	#create classifier, which is a multilayer perceptron that receives the result of an initial convolution
	classifier = MLPDropout(rng, input=layer1_input, layer_sizes=hidden_units, activations=activations, dropout_rates=dropout_rate)

	#define parameters of the model and update functions using adadelta
	params = classifier.params
	for conv_layer in conv_layers:
		params += conv_layer.params

	#if nonStatic, the word2vectors are, themselves, parameters of the model
	if nonStatic:
		params += [wordsRepresentations]

	#computation of COST - provides only y because the object classifier has the prediction answer as property p_y_given_x
	COST = classifier.negative_log_likelihood(y)
	#update scheme
	dropout_cost = classifier.dropout_negative_log_likelihood(y)
	grad_updates = sgd_updates_adadelta(params, dropout_cost, learningDecay, 1e-6, sqr_norm_lim)
	#COST + backpropagation compilation
	train_model = theano.function([x,y], COST, updates=grad_updates,allow_input_downcast=True)
	#---------------------------------------end of main model definition
	#--------------------------------------------------------------------------------------------

	#---------------------------compilation of model just for computing test precision------------
	test_layer0_input = wordsRepresentations[T.cast(x.flatten(),dtype="int32")].reshape((test_size,1,dataLenght_int,wordsRepresentations.shape[1]))
	for conv_layer in conv_layers:
		test_layer0_output = conv_layer.prePrediction(test_layer0_input, test_size)
		test_pred_layers.append(test_layer0_output.flatten(2))
	test_layer1_input = T.concatenate(test_pred_layers, 1)
	finalPredictions = classifier.makeFinalPredictions(test_layer1_input)
#    test_y_pred, test_y_probabilites = classifier.makeFinalPredictions(test_layer1_input)  # ====> final prediction (executed by the compiled function below)

	#test_y_pred is the vector in which each position is the index of the class with highest probability for the sample at that position
	#test_y_probabilites is a matrix in which each row is a line of the probabily vector, one line per sample (of a batch)
	computePercentageOfCorrectlyClassifiedSamplesInTEST_COMPILED = theano.function([x], finalPredictions, allow_input_downcast = True)
	#compilation
	#computePercentageOfCorrectlyClassifiedSamplesInTEST_COMPILED = theano.function([x,y], T.mean(T.eq(test_y_pred, y)), allow_input_downcast = True)
	# ------------------------------------------------------------------------

	# --------------------------------compilation of functions to compute Precision for train and test data
	#precision compilation
	precisionComputationForTrainData_COMPILED = theano.function([x,y], classifier.computePercentageOfCorrectlyClassifiedSamples(y),allow_input_downcast=True)

	#validation
	precisionComputationForValidationData_COMPILED = theano.function([x,y], classifier.computePercentageOfCorrectlyClassifiedSamples(y),allow_input_downcast=True)
	#-----------------------------------------------------------------------------------------------------------------
	# ----------------------------------------------------------------------------------------------------------------

	#-------------------------------start training over mini-batches
	epoch = 0
	bestValidationPerformance = 0
	print '... training'
	while (epoch < n_epochs):
		start_time = time.time()
		epoch = epoch + 1
		#---------------------training only (with back propagation)
		for minibatch_index in numpy.random.permutation(range(n_train_batches)):
			train_model(train_set_x[minibatch_index * batch_size:(minibatch_index + 1) * batch_size], train_set_y[minibatch_index * batch_size:(minibatch_index + 1) * batch_size])
			set_zero(zero_vec)
		#--------------------training is over

		#Computation of performance metric Precision
		#Compute performance as Precision (or percentage of correctly classified samples) for train and validation
		#precision over train data
		percentageOfCorrectlyClassifiedSamples_LIST = []
		for minibatch_index in range(n_train_batches):
			percentageOfCorrectlyClassifiedSamples_LIST.append(precisionComputationForTrainData_COMPILED(train_set_x[minibatch_index * batch_size: (minibatch_index + 1) * batch_size],train_set_y[minibatch_index * batch_size: (minibatch_index + 1) * batch_size]))
		trainPrecision = numpy.mean(percentageOfCorrectlyClassifiedSamples_LIST)

		#precision over validation data
		percentageOfCorrectlyClassifiedSamples_LIST = []
		for minibatch_index in range(n_val_batches):
			percentageOfCorrectlyClassifiedSamples_LIST.append(precisionComputationForValidationData_COMPILED(val_set_x[minibatch_index * batch_size: (minibatch_index + 1) * batch_size],val_set_y[minibatch_index * batch_size: (minibatch_index + 1) * batch_size]))
		valPrecision = numpy.mean(percentageOfCorrectlyClassifiedSamples_LIST)

		#---------------------results reporting
		print('epoch: %i, training time: %.2f secs, train Precision: %.2f %%, val Precision: %.2f %%'
			  % (epoch, time.time() - start_time, trainPrecision * 100., valPrecision * 100.))
		#computed for each epoch that improved the performance of the validation
		#but not incrementally, for all the epochs, just one result corresponding to the best performance
		if valPrecision >= bestValidationPerformance:
			bestValidationPerformance = valPrecision
			test_y_pred, test_y_probabilites = computePercentageOfCorrectlyClassifiedSamplesInTEST_COMPILED(test_set_x)
			# compares the predicted class of each sample with its actual class
			# T.eq compares the vectors of prediction results against the labels
			# the result is a vector in which each position holds 1 or 0 -> 1 predicted class is right, 0 -> it is wrong
			# the error is the mean of this vector -> it is not used for training, only for testing and reporting feedback to the user
			# ==> NOTICE that, since it is a binary vector, its mean corresponds to the percertage of positions holding value 1 = Precision (or accuracy)
			percentageCorrectlyClassifiedSamples = numpy.mean(numpy.equal(test_y_pred, test_set_y))

			#output for trec_eval processing
			inputToTrecEval = workingDirectory + 'inputToTrecEval.txt'
			if os.path.exists(inputToTrecEval):
				os.remove(inputToTrecEval)
			file = open(inputToTrecEval, 'w')
			print '(re) writing ' + inputToTrecEval
			#for each test sample
			previouslyTestedQuestions = set()
			for i in range(0,test_size):
				#these 3 lines avoid that the same question be classified more than once - it happens with the multiple-classes data
				if test_set_qIds[i] in previouslyTestedQuestions:
					continue
				previouslyTestedQuestions.add(test_set_qIds[i])
				ithSampleProbabilitiesAndClass = []
				#for each predicted probability
				for sampleClass, sampleProb in enumerate(test_y_probabilites[i]):
					ithSampleProbabilitiesAndClass.append((sampleClass, sampleProb))
				#order everything
				ithSampleProbabilitiesAndClass = sorted(ithSampleProbabilitiesAndClass,key = lambda x: x[1], reverse=True)
				#output the first 10
				for j in range(0,10):
					file.write(str(test_set_qIds[i])+' QQ '+getLabelFromTypeId(ithSampleProbabilitiesAndClass[j][0])+' '+str(j+1)+' '+str(ithSampleProbabilitiesAndClass[j][1])+' '+'NOEXP'+'\n')
			file.close()

	return percentageCorrectlyClassifiedSamples


def shared_dataset(data_xy, borrow=True):
		""" Function that loads the dataset into shared variables

		The reason we store our dataset in shared variables is to allow
		Theano to copy it into the GPU memory (when code is run on GPU).
		Since copying data into the GPU is slow, copying a minibatch everytime
		is needed (the default behaviour if the data is not in a shared
		variable) would lead to a large decrease in performance.
		"""
		data_x, data_y = data_xy
		shared_x = theano.shared(np.asarray(data_x, dtype=theano.config.floatX), borrow=borrow)
		shared_y = theano.shared(np.asarray(data_y, dtype=theano.config.floatX), borrow=borrow)
		return shared_x, T.cast(shared_y, 'int32')


def sgd_updates_adadelta(params,cost,rho=0.95,epsilon=1e-6,norm_lim=9):
	"""
	adadelta update rule, mostly from
	https://groups.google.com/forum/#!topic/pylearn-dev/3QbKtCumAW4 (for Adadelta)
	"""
	updates = OrderedDict({})
	exp_sqr_grads = OrderedDict({})
	exp_sqr_ups = OrderedDict({})
	gparams = []
	for param in params:
		empty = numpy.zeros_like(param.get_value())
		exp_sqr_grads[param] = theano.shared(value=as_floatX(empty),name="exp_grad_%s" % param.name)
		gp = T.grad(cost, param)

		exp_sqr_ups[param] = theano.shared(value=as_floatX(empty), name="exp_grad_%s" % param.name)
		gparams.append(gp)
	for param, gp in zip(params, gparams):
		exp_sg = exp_sqr_grads[param]
		exp_su = exp_sqr_ups[param]
		up_exp_sg = rho * exp_sg + (1 - rho) * T.sqr(gp)
		updates[exp_sg] = up_exp_sg
		step =  -(T.sqrt(exp_su + epsilon) / T.sqrt(up_exp_sg + epsilon)) * gp
		updates[exp_su] = rho * exp_su + (1 - rho) * T.sqr(step)
		stepped_param = param + step
		if (param.get_value(borrow=True).ndim == 2) and (param.name!='word2vectorMatrix'):
			col_norms = T.sqrt(T.sum(T.sqr(stepped_param), axis=0))
			desired_norms = T.clip(col_norms, 0, T.sqrt(norm_lim))
			scale = desired_norms / (1e-7 + col_norms)
			updates[param] = stepped_param * scale
		else:
			updates[param] = stepped_param
	return updates





def as_floatX(variable):
	if isinstance(variable, float):
		return numpy.cast[theano.config.floatX](variable)

	if isinstance(variable, numpy.ndarray):
		return numpy.cast[theano.config.floatX](variable)
	return theano.tensor.cast(variable, theano.config.floatX)

#def safe_update(dict_to, dict_from):
	#    """
	#re-make update dictionary for safe updating
	#"""
	#for key, val in dict(dict_from).iteritems():
	#        if key in dict_to:
	#       raise KeyError(key)
	#   dict_to[key] = val
	#return dict_to

def get_idx_from_sent(sent, word_idx_map, max_l, filter_h=5):
	"""
	Transforms sentence into a list of indices. Pad with zeroes.
	"""
	x = []
	pad = filter_h - 1
	#make vector start with an initial padding
	for i in xrange(pad):
		x.append(0)
	#for each word, appends its word_idx_map to the vector
	words = sent.split()
	for word in words:
		if word in word_idx_map:
			x.append(word_idx_map[word])
	#make sure every vector has the same lenght by using padding
	while len(x) < max_l+2*pad:
		x.append(0)
	return x

def prepareTrainAndTestForIthCrossValidation(sentences, word_idx_map, iThCrossValidation, max_l, filter_h=5, use8020Split = True):
	"""
	Transforms sentences into a 2-d matrix, in which each word is represented by the respective index in word_idx_map.
	Returns two lists:
	-test with the iThCrossValidation fold set
	-train with listOfCrossValidationFolders-1 (see process_data-LIG.py) cross validation folds
	"""
	train, test, trainqIds, testqIds = [], [], [], []
	if use8020Split: #it is enough to state the test file of questions, the train becomes all the remaining questions
		testDataFrame = pd.read_csv("./NewData/NLPSingleClass/Questions-SingleClass-TEST-10.csv",names=['qId', 'sentence','class'])
	numberOfSentencesInTestDataSet = 0
	for sentence in sentences:
		#Transforms sentence into a list of word_idx_map indices
		sent = get_idx_from_sent(sentence["text"], word_idx_map, max_l, filter_h)
		#append the class, indicated by json field y
		sent.append(sentence["y"])
		# in process_data-LIG.py, each sentence gets a randomly uniform label stating its cross validation partition
		# we use it here to create test and train sets
		#each sentence has a split number - the data was originally prepared with 10 splits uniformily distributed
		#so 10% for each split number

		if use8020Split: #works for 1-fold validation with file of questions to consider as test
			if sentence['qId'] in testDataFrame['qId'].values:
				test.append(sent)
				testqIds.append(sentence['qId'])
				numberOfSentencesInTestDataSet += 1
			else:
				train.append(sent)
				trainqIds.append(sentence["qId"])
		else:  #works for 10-fold cross validation
			if sentence["split"] == iThCrossValidation:
				test.append(sent)
				testqIds.append(sentence["qId"])
			else:
				train.append(sent)
				trainqIds.append(sentence["qId"])
	if use8020Split:
		print 'Sentences found for test: ' + str(numberOfSentencesInTestDataSet)
		print 'Sentences in test list: ' + str(len(testqIds))
		print 'Sentences in train list: ' + str(len(trainqIds))

	#convert lists to arrays
	train_temp = numpy.array(train,dtype="int")
	test_temp = numpy.array(test,dtype="int")
	return [train_temp, test_temp, trainqIds, testqIds]

if __name__=="__main__":
	print "Command line is: python conv_net_sentence-LIG.py input_file.p max_sentence_length(int) mode(-static/-nonstatic) word_vectors(-rand/-word2vec)"
	print ''
	if len(sys.argv) < 4:
		print 'Please, inform (via command line argument) the maximum lenght size for this corpora, the mode (-nonstatic/-static), and the vector type (-rand/-word2vec).'
		print 'Exiting script.'
		sys.exit()
	theano.config.floatX = 'float32'
	global workingDirectory

	print "Remember to set floatX=float32 in file .theanorc"
	print "Remember to set parameter max_l according to the dataset"
	print "Remember to set parameter img_w according to the word2vec preprocessing"
	print ''
	print "loading data...",

	inputPickelFile = sys.argv[1]
	max_sentence_length = int(sys.argv[2])
	mode = sys.argv[3]
	word_vectors = sys.argv[4]

	dataFile = cPickle.load(open(inputPickelFile, "rb"))
	workingDirectory = inputPickelFile + 'RESULT' + mode + word_vectors

	# sentences: all the sentences to train and test, along with their classes, num of words, cross validation split (see process_data-LIG.py)
	# word2vecMatrix: word2vec vectors of each word
	# randomWord2vecMatrix: randon word2vec vectors for each word -> the softare uses either word2vecMatrix or randomWord2vecMatrix
	# word_idx_map: map word->index; the index works both on word2vecMatrix and randomWord2vecMatrix indicating the vector of each word
	# vocab: all the words, along with their document frequency (number of sentences in which they appear)
	sentences, word2vecMatrix, randomWord2vecMatrix, word_idx_map, vocab = dataFile[0], dataFile[1], dataFile[2], dataFile[3], dataFile[4]

	print "data loaded!"
	execfile("conv_net_classes-LIG.py")

	if mode=="-nonstatic":
		print "model architecture: CNN-non-static"
		nonStatic=True
	elif mode=="-static":
		print "model architecture: CNN-static"
		nonStatic=False

	if word_vectors=="-rand":
		print "Not using word2vectors, but only random vectors"
		U = randomWord2vecMatrix
	elif word_vectors=="-word2vec":
		print "Using word2vec vectors"
		U = word2vecMatrix

	results = []
	listOfCrossValidationFolders = range(0, 1)

	n_epochs = 50
	print 'Start of ' + str(len(listOfCrossValidationFolders)) + ' cross validation executions, each one with ' + str(n_epochs) + ' epochs'
	for iThCrossValidation in listOfCrossValidationFolders:
		#prepares the data - Transforms sentences into a 2-d matrix, in which each word is represented by the respective index in word_idx_map
		#vector representations not used yet
		trainAndTestData_array = prepareTrainAndTestForIthCrossValidation(sentences, word_idx_map, iThCrossValidation, max_l=max_sentence_length, filter_h=5, use8020Split = True)
		#model building, training, and testing - the way the code was written performs building for every cross validation
		precisionOverTestSetAfterTraining = train_conv_net(
															trainAndTestData_array,
															U,
															n_epochs,
															img_w = 50,
															learningDecay=0.95,
															filter_hs=[3,4,5],
															activationFunction="relu",
															hidden_units=[50,362], #31 is the number of classes
															sqr_norm_lim=9,
															nonStatic=nonStatic,
															batch_size=50,
															dropout_rate=[0.5])
		print "Cross validation number: " + str(iThCrossValidation) + ", Precision: " + str(precisionOverTestSetAfterTraining)
		print "Precision = percentage of correctly predicted classes."
		results.append(precisionOverTestSetAfterTraining)
	print 'Average precision of the ' + str(listOfCrossValidationFolders) + ' cross validations: ' + str(numpy.mean(results))