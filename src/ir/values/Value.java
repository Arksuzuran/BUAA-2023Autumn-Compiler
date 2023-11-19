package ir.values;

import ir.types.ValueType;

import java.util.ArrayList;

/**
 * @Description 基本的Value类
 * @Author
 * @Date 2023/10/30
 **/
public class Value {

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNameCnt(){
        return name.substring(1);
    }

    public ValueType getType() {
        return type;
    }

    public Value getParent() {
        return parent;
    }

    public ArrayList<User> getUsers() {
        return users;
    }
    public boolean isArg() {
        return isArg;
    }
    public int getArgNumber() {
        return argNumber;
    }

    /**
     * 唯一标识
     */
    private final int id;

    /**
     * 当前value的虚拟寄存器名称
     */
    private String name;

    /**
     * 当前Value的类型
     */
    private ValueType type;
    /**
     * 包含当前value的父value
     * 例如Instruction包含在BasicBlock里
     */
    private Value parent;
    /**
     * 使用当前Value的User
     */
    private final ArrayList<User> users = new ArrayList<>();
    /**
     * 当前value是否作为函数参数
     */
    private boolean isArg = false;  // 是否是函数参数
    private int argNumber = 0;      // 第几个参数, 从0开始
    public Value(String name, ValueType type, Value parent) {
        this.id = applyNewId();
        this.name = name;
        this.type = type;
        this.parent = parent;
    }
    public Value(String name, ValueType type, Value parent, int argNumber) {
        this.id = applyNewId();
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.isArg = true;
        this.argNumber = argNumber;
    }

    /**
     * 向user列表内添加user
     * @param user  要添加的user
     */
    public void adduser(User user){
        this.users.add(user);
    }
    public void removeUser(User user) {
        users.remove(user);
    }
    private static int idCnt = 0;
    /**
     * 申请新的独特id
     * @return  新的id
     */
    private static int applyNewId(){
        return idCnt++;
    }
    public static void setIdCntZero(){
        idCnt = 0;
    }

    /**
     * 重命名一个value
     * @param name  新name
     */
    public void rename(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return type + " " + name;
    }

    public void buildMips(){
        System.out.println("Value类: buildMips");
    }
}
