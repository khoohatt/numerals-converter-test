import ru.textanalysis.tawt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tawt.jmorfsdk.JMorfSdkFactory;
import ru.textanalysis.tawt.ms.grammeme.MorfologyParameters;
import ru.textanalysis.tawt.ms.model.jmorfsdk.Form;
import java.util.Scanner;

public class Main {
    // ыгр, 76676! вора. ыв - 666, 88, ооыова.
    // Есть заявки на 2 конфедерации и 1 мусульманское государство.
    // Ледокол обошёл Антарктиду за 62 суток.
    // как-то 5-ти раз-таки

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String string = " ";

        while (!string.equals("")) {
            System.out.print("\nтекст: ");
            string = scanner.nextLine();
            System.out.println("результат: " + replaceNumber(string));
        }
    }

    public static String replaceNumber(String text) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
        long[] forms = new long[2];  // падеж, род

        String[] words = text.split("(?<![\\p{L}\\p{N}_-])|(?![\\p{L}\\p{N}_-])");
        String[] result = new String[words.length];
        
        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (word.matches("\\d+")) {
                if (words.length > i + 1) {
                    System.out.println(words[i + 2]);
                    for (Form form : jMorfSdk.getOmoForms(words[i + 2])) {
                        System.out.println("слово: " + form);

                        if (form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN
                                && (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Numbers.class) == MorfologyParameters.Numbers.PLURAL
                                ^ Integer.parseInt(word) % 10 == 1)) {
                            System.out.println("существительное: " + form);
                            forms[0] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER);
                            forms[1] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class);
//                            if (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == MorfologyParameters.Case.NOMINATIVE) {
//                                break;
//                            }
                        }
                    }
                }
                result[i] = convert_number_to_words_ru(Integer.parseInt(word), forms);
                
            } else if (word.matches("\\d+-(?:[йяе]|(?:[тм]и))")) {
                result[i] = convert_number_to_words_ru(Integer.parseInt(word.substring(0, word.indexOf("-"))), forms, word.substring(word.indexOf("-") + 1));
            } else if (word.matches("\\d+,\\d+")) {
                System.out.println(words[i + 1]);
                jMorfSdk.getOmoForms(words[i + 1]).forEach((form) -> {

                    if (form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN) {
                        System.out.println("существительное: " + form);
                        forms[0] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER);
                        forms[1] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class);
                    }
                });
                result[i] = convert_number_to_words_ru(Integer.parseInt(word.replace(",", ".")), forms);

            } else if (word.matches("\\d+[.]\\d+")) {
                System.out.println(words[i + 1]);
                jMorfSdk.getOmoForms(words[i + 1]).forEach((form) -> {

                    if (form.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN) {
                        System.out.println("существительное: " + form);
                        forms[0] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER);
                        forms[1] = form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class);
                    }
                });
                result[i] = convert_number_to_words_ru(Integer.parseInt(word.replace(".", "")), forms);

            } else {
                result[i] = word;
            }
        }
//        jMorfSdk.finish();

        StringBuilder finalText = new StringBuilder();
        for (int i = 0; i < result.length; i++) {
            finalText.append(result[i]);
        }

        return finalText.toString();
    }

    public static String convert_number_to_words_ru(int num, long[] forms, String... buildup) {
        String[] ones = {"ноль", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать"};
        String[] tens = {"", "", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};
        String[] hundreds = {"", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"};
        String[] thousands = {"тысяча", "тысячи", "тысяч"};
        String[] millions = {"миллион", "миллиона", "миллионов"};

        String[] onesOrdinal = {"нулевой", "первый", "второй", "третий", "четвертый", "пятый", "шестой", "седьмой", "восьмой", "девятый", "десятый", "одиннадцатый", "двенадцатый", "тринадцатый", "четырнадцатый", "пятнадцатый", "шестнадцатый", "семнадцатый", "восемнадцатый", "девятнадцатый"};
        String[] tensOrdinal = {"", "", "двадцатый", "тридцатый", "сороковой", "пятидесятый", "шестидесятый", "семидесятый", "восьмидесятый", "девяностый"};
        String[] hundredsOrdinal = {"", "сотый", "двухсотый"};

        StringBuilder result = new StringBuilder();

        if (num / 1000000 > 0) {
            result.append(convert_number_to_words_ru(num / 1000000, forms)).append(" ").append(getWordForms(num / 1000000, millions, forms[0])).append(" ");
            num = num % 1000000;
        }

        if (num / 1000 > 0) {
            result.append(convert_number_to_words_ru(num / 1000, new long[]{forms[0] != 0 ? forms[0] : 512, 8})).append(" ").append(getWordForms(num / 1000, thousands, forms[0])).append(" ");
            num = num % 1000;  // getWordForms(num / 1000, thousands, forms[1])  // thousands[(num / 1000) % 10])
        }

        if (num / 100 > 0) {
            result.append(getFormedNumber(forms, hundreds[num / 100])).append(" ");
            num = num % 100;
        }

        if (num > 0) {
            if (num < 20) {
                if (buildup.length == 0) {
                    result.append(getFormedNumber(forms, ones[num])).append(" ");
                } else {
                    result.append(getFormedNumberWithBuildup(getFormedNumber(forms, ones[num]), buildup[0]));
                }
            } else {
                if (buildup.length == 0) {
                    result.append(getFormedNumber(forms, tens[num / 10])).append(" ");
                } else {
                    result.append(getFormedNumberWithBuildup(getFormedNumber(forms, ones[num / 10]), buildup[0]));
                }
                
                if (num % 10 > 0) {
                    if (buildup.length == 0) {
                        result.append(getFormedNumber(forms, ones[num % 10])).append(" ");
                    } else {
                        result.append(getFormedNumberWithBuildup(getFormedNumber(forms, ones[num % 10]), buildup[0]));
                    }
                }
            }
        }

        return result.toString().trim();
    }

    private static String getWordForms(int value, String[] forms, long identifier) {
        String result = "";
        if (value % 100 >= 11 && value % 100 <= 19) {
            result = forms[2];
        } else if (value % 10 == 1) {
            result = forms[0];
        } else if (value % 10 >= 2 && value % 10 <= 4) {
            result = forms[1];
        } else {
            result = forms[2];
        }
        
/*        String result = forms[0];
        System.out.println("форма: " + identifier);
        if (identifier != 0) {
            JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();
            final String[] number = {""};
            jMorfSdk.getOmoForms(result).forEach((form) -> {
                if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == identifier)) {
                    System.out.println("порядок: " + form);
                    number[0] = form.getMyString();
                }
            });
            if (number[0].length() > 0) {
                System.out.println("форма: " + number[0]);
                return number[0];
            }
        } */
        return result;
    }

    private static String getFormedNumber(long[] forms, String num) {
        if (forms[0] != 0) {
            JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

//            18:03:36.505 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: один с частью речи: 17
//            18:03:36.507 [main] DEBUG ru.textanalysis.tawt.jmorfsdk.JMorfSdkImpl - В словаре отсутствует производное слов, слова: один с частью речи: 28
            byte param;
            if (num.equals("один")) {
                param = MorfologyParameters.TypeOfSpeech.NOUN;
            } else {
                param = MorfologyParameters.TypeOfSpeech.NUMERAL;
            }

            for (String s : jMorfSdk.getDerivativeFormLiterals(num, param)) {
                System.out.println(s);
                for (Form form : jMorfSdk.getOmoForms(s)) {
                    if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Case.IDENTIFIER) == forms[0])) {
                        if ((form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == 0)
                                || (form.getMorfCharacteristicsByIdentifier(MorfologyParameters.Gender.class) == forms[1])) {
                            System.out.println("число: " + form);
                            return form.getMyString();
                        }
                    }
                }
            }
            return "";
        } else {
            return num;
        }
    }

    private static String getFormedNumberWithBuildup(String num, String buildup) {
        JMorfSdk jMorfSdk = JMorfSdkFactory.loadFullLibrary();

        for (String s : jMorfSdk.getDerivativeFormLiterals(num, MorfologyParameters.TypeOfSpeech.NUMERAL)) {
            System.out.println(s);
            for (Form form : jMorfSdk.getOmoForms(s)) {
                System.out.println("наращение в методе: " + form.getMyString().substring(buildup.length()));
                if (form.getMyString().substring(buildup.length()).endsWith(buildup)) {
                    System.out.println("число: " + form);
                    return form.getMyString();
                }
            }
        }
        
        //  порядковые не нашлись в библиотеке, про собирательные думаю
        return "";
    }
}
