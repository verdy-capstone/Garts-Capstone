import json
import pickle
import random
import nltk
import numpy
from nltk.stem import LancasterStemmer
from tensorflow import lite
from tensorflow.python.keras.layers import Dense
from tensorflow.python.keras.models import Sequential
from tensorflow.python.keras.models import model_from_yaml

nltk.download('punkt')
nltk.download('wordnet')

stemmer = LancasterStemmer()

with open("intents.json") as file:
    data = json.load(file)

try:
    with open("chatbot.pickle", "rb") as file:
        kata, label, train, hasil = pickle.load(file)

except:
    kata = []
    label = []
    doc_x = []
    doc_y = []

    for intent in data["intents"]:
        for pattern in intent["patterns"]:
            kta = nltk.word_tokenize(pattern)
            kata.extend(kta)
            doc_x.append(kta)
            doc_y.append(intent["tag"])

        if intent["tag"] not in label:
            label.append(intent["tag"])

    kata = [stemmer.stem(w.lower()) for w in kata if w != "?"]
    kata = sorted(list(set(kata)))

    label = sorted(label)

    train = []
    hasil = []

    output_empty = [0 for _ in range(len(label))]

    for x, doc in enumerate(doc_x):
        bag = []

        kta = [stemmer.stem(w.lower()) for w in doc]

        for w in kata:
            if w in kta:
                bag.append(1)
            else:
                bag.append(0)

        output_row = output_empty[:]
        output_row[label.index(doc_y[x])] = 1

    train.append(bag)
    hasil.append(output_row)

    train = numpy.array(train)
    hasil = numpy.array(hasil)

    with open("chatbot.pickle", "wb") as file:
        pickle.dump((kata, label, train, hasil), file)

try:
    yamlfile = open('chatbotmodel.yaml', 'r')
    loadedmodel_yaml = yamlfile.read()
    yamlfile.close()
    ChatbotModel = model_from_yaml(loadedmodel_yaml)
    ChatbotModel.load_weights("chatbotmodel.h5")
    print("Load the model from disk")

except:
    ChatbotModel = Sequential()
    ChatbotModel.add(Dense(8, input_shape=[len(kata)], activation='relu'))
    ChatbotModel.add(Dense(len(label), activation='softmax'))

    ChatbotModel.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

    ChatbotModel.fit(train, hasil, epochs=1000, batch_size=8)

    modelyaml = ChatbotModel.to_yaml()
    with open("chatbotmodel.yaml", "w") as y_file:
        y_file.write(modelyaml)

    ChatbotModel.save_weights("chatbotmodel.h5")
    print("Saved model from disk")

converter = lite.TFLiteConverter.from_keras_model(ChatbotModel)
tfChatbotModel = converter.convert()

open('ChatbotModel.tflite', 'wb').write(tfChatbotModel)

def bag_of_words(s, kata):
    bag = [0 for _ in range(len(kata))]

    s_words = nltk.word_tokenize(s)
    s_words = [stemmer.stem(word.lower()) for word in s_words]

    for se in s_words:
        for i, w in enumerate(kata):
            if w == se:
                bag[i] = 1

    return numpy.array(bag)


def chatWithBot(inputText):
    currentText = bag_of_words(inputText, kata)
    currentTextArray = [currentText]
    numpyCurrentText = numpy.array(currentTextArray)

    if numpy.all((numpyCurrentText == 0)):
        return "I didn't get that, try again"

    result = ChatbotModel.predict(numpyCurrentText[0:1])
    result_index = numpy.argmax(result)
    tag = label[result_index]

    if result[0][result_index] > 0.7:
        for tg in data["intents"]:
            if tg['tag'] == tag:
                responses = tg['responses']

        return random.choice(responses)

    else:
        return "I didn't get that, try again"


def chat():
    print("Start asking your question with us (You may type quit to stop)")

    while True:
        inp = input("You : ")
        if inp.lower() == "quit":
            break

        print(chatWithBot(inp))

chat()