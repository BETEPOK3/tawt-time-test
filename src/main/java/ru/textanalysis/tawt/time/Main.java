package ru.textanalysis.tawt.time;

import ru.textanalysis.tawt.gama.Gama;
import ru.textanalysis.tawt.graphematic.parser.text.GParserImpl;
import ru.textanalysis.tawt.graphematic.parser.text.GraphematicParser;
import ru.textanalysis.tawt.gama.GamaImpl;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdkFactory;
import ru.textanalysis.tawt.ms.model.gama.BearingPhrase;
import ru.textanalysis.tawt.ms.model.sp.Sentence;
import ru.textanalysis.tawt.sp.api.SyntaxParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static final int TEST_CYCLE_COUNT = 100;
    private static final int TEST_FILES_COUNT = 23;

    public static void main(String[] args) {
        //JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

        //timeGraphParserText();
        //timeGamaGetMorphBearingPhrase();
        //timeGamaDisambiguation(false);
        //timeGamaDisambiguation(true);
        redistributeSentencesByWordCount();
        //timeSyntaxGetTreeSentence();
    }

    /**
     * Вычисление времени выполнения графематического анализа.
     * Метод parserText класса GParserImpl модуля graphematic-parser.
     */
    private static void timeGraphParserText() {
        GParserImpl parser = new GParserImpl();

        try {
            BufferedWriter wr = new BufferedWriter(new FileWriter("time-test/graphematic.parserText.csv"));
            wr.write("Length;Word count;Time, ms\n");

            for (int i = 1; i <= TEST_FILES_COUNT; ++i) {
                // Подготовка входных данных
                String filename = String.format("time-test/graph-test-input/text%02d.txt", i);
                String content = Files.readString(Path.of(filename));
                List<List<List<List<String>>>> testResult = null;
                long elapsed = 0;

                // Прогон функции некоторое кол-во раз
                for (int j = 1; j < TEST_CYCLE_COUNT; ++j) {
                    long start = System.nanoTime();
                    testResult = parser.parserText(content);
                    elapsed += System.nanoTime() - start;
                }

                // Расчёт кол-ва слов
                int wordCount = 0;
                for (List<List<List<String>>> lst1 : testResult) {
                    for (List<List<String>> lst2 : lst1) {
                        for (List<String> lst3 : lst2) {
                            wordCount += lst3.size();
                        }
                    }
                }

                // Расчёт времени выполнения и сохранение результатов в файл
                elapsed /= TEST_CYCLE_COUNT;
                System.out.printf("Text size: %d chars\tWords count: %d\tTook to parse: %f ms%n",
                        content.length(), wordCount, (double)elapsed / 1000000.0);
                wr.write(String.format("%d;%d;%f\n", content.length(), wordCount, (double)elapsed / 1000000.0));
            }

            wr.close();
        }
        catch (IOException exc) {
            System.out.println("Что-то пошло не так.");
        }
    }

    /**
     * Вычисление времени выполнения графематического + морфологического анализа.
     * Метод getMorphBearingPhrase класса GamaImpl модуля gama.
     */
    private static void timeGamaGetMorphBearingPhrase() {
        GamaImpl gamaImpl = new GamaImpl();
        gamaImpl.init();

        try {
            BufferedWriter wr = new BufferedWriter(new FileWriter("time-test/gama.getMorphBearingPhrase.csv"));
            wr.write("Length;Word count;Time, ms\n");

            for (int i = 1; i <= TEST_FILES_COUNT; ++i) {
                // Подготовка входных данных
                String filename = String.format("time-test/graph-test-input/text%02d.txt", i);
                String content = Files.readString(Path.of(filename));
                BearingPhrase testResult = null;
                long elapsed = 0;

                // Прогон функции некоторое кол-во раз
                for (int j = 1; j < TEST_CYCLE_COUNT; ++j) {
                    long start = System.nanoTime();
                    testResult = gamaImpl.getMorphBearingPhrase(content);
                    elapsed += System.nanoTime() - start;
                }

                // Расчёт времени выполнения и сохранение результатов в файл
                elapsed /= TEST_CYCLE_COUNT;
                int wordCount = testResult.getWords().size();
                System.out.printf("Text size: %d chars\tWords count: %d\tTook to parse: %f ms%n",
                        content.length(), wordCount, (double)elapsed / 1000000.0);
                wr.write(String.format("%d;%d;%f\n", content.length(), wordCount, (double)elapsed / 1000000.0));
            }

            wr.close();
        }
        catch (IOException exc) {
            System.out.println("Что-то пошло не так.");
        }
    }

    /**
     * Вычисление времени выполнения частичного снятия морфологической омонимии.
     * Метод disambiguation класса GamaImpl модуля gama.
     *
     * @param initDisambiguationResolver - инициализировать ли инструмент снятия морфологической омонимии.
     */
    private static void timeGamaDisambiguation(boolean initDisambiguationResolver) {
        GamaImpl gamaImpl = new GamaImpl();
        gamaImpl.init(initDisambiguationResolver);

        try {
            BufferedWriter wr = new BufferedWriter(new FileWriter(
                    String.format("time-test/gama.disambiguation.init.%b.csv", initDisambiguationResolver)));
            wr.write("Length;Word count;Time, ms\n");

            for (int i = 1; i <= TEST_FILES_COUNT; ++i) {
                // Подготовка входных данных
                String filename = String.format("time-test/graph-test-input/text%02d.txt", i);
                String content = Files.readString(Path.of(filename));
                BearingPhrase testResult = null;
                long elapsed = 0;

                // Прогон функции некоторое кол-во раз
                for (int j = 1; j < TEST_CYCLE_COUNT; ++j) {
                    testResult = gamaImpl.getMorphBearingPhrase(content);
                    long start = System.nanoTime();
                    gamaImpl.disambiguation(testResult);
                    elapsed += System.nanoTime() - start;
                }

                // Расчёт времени выполнения и сохранение результатов в файл
                elapsed /= TEST_CYCLE_COUNT;
                int wordCount = testResult.getWords().size();
                System.out.printf("Text size: %d chars\tWords count: %d\tTook to parse: %f ms%n",
                        content.length(), wordCount, (double)elapsed / 1000000.0);
                wr.write(String.format("%d;%d;%f\n", content.length(), wordCount, (double)elapsed / 1000000.0));
            }

            wr.close();
        }
        catch (IOException exc) {
            System.out.println("Что-то пошло не так.");
        }
    }

    /**
     *  Распределение предложений из разных текстов в разные файлы в зависимости от числа слов в них.
     */
    private static void redistributeSentencesByWordCount() {
        GParserImpl parser = new GParserImpl();
        HashMap<Integer, List<String>> sentenceListMap = new HashMap<>();

        try {
            try (var files = Files.walk(Paths.get("time-test/text-input"))) {
                var finalFiles = files.filter(Files::isRegularFile).toList();

                for (var file : finalFiles) {
                    if (!file.toFile().isFile()) {
                        continue;
                    }

                    String content = Files.readString(file);

                    // Подсчёт кол-ва слов\предложений\средней длины текста
                    var parserResult = parser.parserText(content);
                    for (var paragraph : parserResult) {
                        for (var sentence : paragraph) {
                            int wordCount = 0;
                            StringBuilder str = new StringBuilder();

                            for (int j = 0; j < sentence.size(); j++) {
                                var phrase = sentence.get(j);
                                wordCount += phrase.size();
                                str.append(" ").append(String.join(" ", phrase));
                                if (j < sentence.size() - 1) {
                                    str.append(",");
                                }
                            }
                            str.append(".");

                            var list = sentenceListMap.get(wordCount);
                            if (list == null) {
                                list = new ArrayList<String>();
                                list.add(str.toString());
                                sentenceListMap.put(wordCount, list);
                            }
                            else {
                                list.add(str.toString());
                            }
                        }
                    }
                }
            }

            for (Map.Entry<Integer, List<String>> entry : sentenceListMap.entrySet()) {
                System.out.printf("Sentence with word count %02d count: %d\n", entry.getKey(), entry.getValue().size());

                if (entry.getValue().size() >= 500) {
                    String filename = String.format("time-test/sp-test-input/text%02d.txt", entry.getKey());
                    BufferedWriter wr = new BufferedWriter(new FileWriter(filename));

                    for (String sentence : entry.getValue()) {
                        wr.write(sentence);
                    }

                    wr.close();
                }
            }
        }
        catch (IOException exc) {
            System.out.printf("Что-то пошло не так: %s\n", exc.getMessage());
        }
    }

    /**
     * Вычисление времени выполнения графематического + морфологического + синтаксического анализа.
     */
    private static void timeSyntaxGetTreeSentence() {
        GParserImpl parser = new GParserImpl();
        SyntaxParser syntaxParser = new SyntaxParser();
        syntaxParser.init();

        try {
            BufferedWriter wr = new BufferedWriter(new FileWriter("time-test/syntax.getTreeSentence.csv"));
            wr.write("Length;Word count;Sentence count;Average sentence length;Time, ms\n");

            for (int i = 2; i <= 46; ++i) {
                // Подготовка входных данных
                String filename = String.format("time-test/sp-test-input/text%02d.txt", i);
                String content = Files.readString(Path.of(filename));

                // Подсчёт кол-ва слов\предложений\средней длины текста
                int wordCount = 0;
                int sentenceCount = 0;
                int averageSentenceLength = 0;
                var parserResult = parser.parserText(content);
                for (var paragraph : parserResult) {
                    for (var sentence : paragraph) {
                        ++sentenceCount;
                        for (var phrase : sentence) {
                            wordCount += phrase.size();
                        }
                    }
                }
                averageSentenceLength = wordCount/sentenceCount;

                Sentence testResult;
                long elapsed = 0;

                // Прогон функции некоторое кол-во раз
                for (int j = 1; j < TEST_CYCLE_COUNT; ++j) {
                    long start = System.nanoTime();
                    testResult = syntaxParser.getTreeSentence(content);
                    elapsed += System.nanoTime() - start;
                }

                // Расчёт времени выполнения и сохранение результатов в файл
                elapsed /= TEST_CYCLE_COUNT;
                System.out.printf("Text size: %d chars\tWords count: %d\tSentence count: %d" +
                                "\tAverage sentence length: %d\tTook to parse: %f ms%n",
                        content.length(), wordCount, sentenceCount, averageSentenceLength, (double)elapsed / 1000000.0);
                wr.write(String.format("%d;%d;%d;%d;%f\n", content.length(), wordCount, sentenceCount, averageSentenceLength, (double)elapsed / 1000000.0));
            }

            wr.close();
        }
        catch (IOException exc) {
            System.out.println("Что-то пошло не так.");
        }
    }
}