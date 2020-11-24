import java.nio.charset.CoderMalfunctionError;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        String text = "Wild boar Wasyok";

        TreeMap<Character, Integer> frequencies = countFrequency(text);

        //список узлов для листов дерева
        ArrayList<codeTreeNode> codeTreeNodes = new ArrayList<>();
        for(Character c : frequencies.keySet()){
            //генерация узлов
            codeTreeNodes.add(new codeTreeNode(c, frequencies.get(c)));
        }

        //Строим кодовое дерево
        codeTreeNode tree = huffman(codeTreeNodes);

        //Генерация кодов для каждого символа
        TreeMap<Character, String> codes = new TreeMap<>();
        for(Character c : frequencies.keySet()){
            codes.put(c, tree.getCodeForCharacter(c, ""));
        }

        System.out.println("Таблица префиксных кодов: " + codes.toString());

        //Кодирование строки с помощью сгенерированных кодов
        StringBuilder encoded = new StringBuilder();
        //Проходим по строке и для каждого символа в StringBuilder записываем код этого символа, которое сгенерировалось кодовым деревом
        for(int i = 0; i < text.length(); i++){
            encoded.append(codes.get(text.charAt(i)));
        }

        System.out.println("Размер исходной строки: " + text.getBytes().length * 8 + " бит");
        System.out.println("Размер сжатой строки: " + encoded.length() + " бит");
        System.out.println("Биты сжатой строки: " + encoded);

        String decoded = huffmanDecode(encoded.toString(), tree);

        System.out.println("Расшифровано: " + decoded);
    }

    //Подсчет того, сколько раз какой символ встречается в тексте (ключом является символ, а данными количество того, сколько раз символ встречается в тексте)
    public static TreeMap<Character, Integer> countFrequency(String text){
        TreeMap<Character, Integer> freqMap = new TreeMap<>();

        for(int i = 0; i < text.length(); i++){
            Character c = text.charAt(i);
            Integer count = freqMap.get(c);
            freqMap.put(c, count != null ? count + 1 : 1);
        }

        return freqMap;
    }

    //Алгоритм Хаффмана (функция будет возвращать дерево и в качестве аргументов принимает список узлов для листов дерева с символами
    public static codeTreeNode huffman(ArrayList<codeTreeNode> codeTreeNodes){
        while(codeTreeNodes.size() > 1){
            //Упорядочивание узлов по весам
            Collections.sort(codeTreeNodes);

            //Берем два узла с самыми маленькими весами
            codeTreeNode left = codeTreeNodes.remove(codeTreeNodes.size() - 1); //Получаем из списка узел и тут же его удаляем
            codeTreeNode right = codeTreeNodes.remove(codeTreeNodes.size() - 1);

            //Промежуточный узел
            codeTreeNode parent = new codeTreeNode(null, right.weight + left.weight, right, left);
            codeTreeNodes.add(parent);
        }

        return codeTreeNodes.get(0);
    }

    //Метод декодирования
    private static String huffmanDecode(String encoded, codeTreeNode treeNode){
        StringBuilder decoded = new StringBuilder(); //накапливаем расшифрованные данные

        //Хранение текущего узла при спуске по дереву
        codeTreeNode node = treeNode;
        //идем по битам зашифрованной строки
        for(int i = 0; i < encoded.length(); i++){
            node = encoded.charAt(i) == '0' ? node.left : node.right;

            if(node.content != null){
                decoded.append(node.content);
                node = treeNode;
            }
        }

        return decoded.toString();
    }


    //Кодовое дерево
    private static class codeTreeNode implements Comparable<codeTreeNode>{

        Character content; //символ
        int weight; //вес (сумма дочерних узлов для промежуточного узла)
        codeTreeNode left, right; //потомки

        public codeTreeNode(Character content, int weight) {
            this.content = content;
            this.weight = weight;
        }

        public codeTreeNode(Character content, int weight, codeTreeNode left, codeTreeNode right) {
            this.content = content;
            this.weight = weight;
            this.left = left;
            this.right = right;
        }

        @Override
        public int compareTo(codeTreeNode codeTreeNode){
            return codeTreeNode.weight - weight; //у кого веса больше, тот и на первом месте (сортировка по убыванию)
        }

        //Проход по дереву от корня до конкретного символа и при этом по поворотам вычислять 0 и 1, которые будут кодом данного символа
        public String getCodeForCharacter(Character ch, String parentPath){
            //параметры функции принимают символ, для которого ищется код и путь в виде 0 и 1

            if(content == ch){
                return parentPath;
            } else {
                if(left != null){
                    String path = left.getCodeForCharacter(ch, parentPath + 0);

                    if(path != null){
                        return path;
                    }
                }

                if(right != null){
                    String path = right.getCodeForCharacter(ch, parentPath + 1);

                    if(path != null){
                        return path;
                    }
                }
            }

            return null;

            //Алгоритм обхода дерева в глубину. Для всех листов, которые не соответсвуют поиску, будет возвращен null в качестве кода и только для одного узла, который нужен вернется какой-то код и он при обходе по дереву всплывет наверх и будет возвращен в качестве результата вызова верхней функции
        }
    }
}
