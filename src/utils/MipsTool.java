package utils;

import backend.instructions.MipsInstruction;
import backend.operands.MipsOperand;
import backend.operands.MipsRealReg;
import backend.operands.MipsVirtualReg;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @Description TODO
 * @Author
 * @Date 2023/11/19
 **/
public class MipsTool {
    public static boolean isReg(MipsOperand op){
        return op instanceof MipsRealReg || op instanceof MipsVirtualReg;
    }

    // 在指定元素后插入新元素的方法
    public static void insertAfter(LinkedList<MipsInstruction> list, MipsInstruction target, MipsInstruction newElement) {
        ListIterator<MipsInstruction> iterator = list.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(target)) {
                iterator.add(newElement);
//                System.out.println("[insertAfter] " + target + ", " + newElement);
                break;
            }
        }
    }
    // 在指定元素前插入新元素的方法
    public static void insertBefore(LinkedList<MipsInstruction> list, MipsInstruction target, MipsInstruction newElement) {
        ListIterator<MipsInstruction> iterator = list.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(target)) {
                iterator.previous(); // 回到目标元素的前一个位置
                iterator.add(newElement);
//                System.out.println("[insertBefore] " + target + ", " + newElement);
                break;
            }
        }
    }
}
