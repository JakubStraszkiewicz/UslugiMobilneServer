package business.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Comparator;
import java.util.UUID;

@Getter
@Setter
public class Operation implements Serializable, Comparable<Operation>  {

    private static int[] createIndices(Operation[] array) {
        int[] intArray = new int[array.length];
        int counter = 0;

        for(Operation operation : array) {

            switch (operation.getName())
            {
                case Operations.REMOVE:
                    intArray[counter] = 1;
                    break;
                case Operations.SAVE:
                    intArray[counter] = 2;
                    break;
                case Operations.INCREASE:
                    intArray[counter] = 3;
                    break;
                case Operations.DECREASE:
                intArray[counter] = 4;
                break;
            }
            counter++;
        }

        return intArray;
    }

    @Override
    public int compareTo(Operation o) {
        Operation[] operations = {this,o};
        int intArray[] = createIndices(operations);

        return Integer.compare(intArray[0],intArray[1]);
    }

    public static class Operations {
        public static final String SAVE = "save product";
        public static final String UPDATE = "update product";
        public static final String REMOVE = "remove product";
        public static final String INCREASE = "increase quantity of product";
        public static final String DECREASE = "decrease quantity of product";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private Product product;

    private Integer difference;
}
