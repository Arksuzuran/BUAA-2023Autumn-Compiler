# 中间代码生成

### 1. 整体思路

LLVM是一种三地址码，即一条LLVM语句可以表示为如下形式：

```llvm
<运算结果> = <指令类型> <操作数1>, <操作数2>
```

观察这种指令可以发现，一条语句主要由三个要素组成：

（1）操作数（2）指令类型（3）运算结果

现在**我们需要将LLVM的这种语言特性，使用Java的类设计来表达**：

1. **在Java代码中，使用具体的类对象，来表示语句中的各个元素**。（就像语法分析中使用各种`node`类来表达各种语法元素一样）

2. 具体措施是，**通过遍历之前语法分析的语法树，采用递归下降的方式，来生成llvm的语法树**。

### 2. 主体架构设计

在语法树中，关键节点类的设计如下：

**空心粗箭头表示类继承关系**（User和Value类也是继承关系，图应该是画错了）。

**实心细箭头表示聚合关系，从而形成树结构**（例如每一个基本块`BasicBlock`里有一条条指令`Instruction`）。

![image-20231110104112393](https://arksuzuran.oss-cn-beijing.aliyuncs.com/img/md_img/image-20231110104112393.png)

```llvm
<运算结果> = <指令类型> <操作数1>, <操作数2>
```

#### 2.1 操作数 Value类

将操作数表示为一个类：Value，它**表示<big>能够作为操作数</big>的对象**。

例如如下乘法语句中：

```
%2 = mul i32 %1, 2
```

按照上面的设计，**`%1`和`2`都是Value，在Java代码中都以Value类的形式存在**。

#### 2.2 操作数使用者 User类

将指令的运算结果表示为一个类：User，它**表示<big>能够作为运算结果</big>的对象，或者说是操作数的使用者**。

例如如下乘法语句中：

```
%2 = mul i32 %1, 2
```

按照上面的设计，`%2`是User，它`use`了`%1`和`2`。**`%2`在Java代码中以User类的形式存在**。

#### 2.3 User类继承Value类

观察如下语句：

```
%2 = mul i32 %1, 2	
%3 = add i32 %2, 3	
```

乘法产生的`%2`**运算结果**在下一条加法语句中**作为了操作数**。`User`类应该继承`Value`类，这样后面的运算才能用到前面的结果。

#### 2.4 指令 Instruction类

为每一种指令类型都创建一个`Instruction`指令类，这个**指令类继承`User`，既用来表示运算结果，也用来表示这一条指令**。

如何理解"既用来表示运算结果，也用来表示这一条指令"？

回到`2.User类`的例子

```
%2 = mul i32 %1, 2
```

在代码实现当中，**`%2`是一个`Mul`指令类对象**。

- 它既**表示`%2`这个运算结果**：Mul extends User，这意味着这个`Mul`类对象可以表示运算结果；User extends Value，这意味着这个`Mul`类对象还可以作为操作数。
- 又**表示这条语句本身**：Mul类是llvm语法树中，挂在BasicBlock下的一个节点，通过遍历语法树，并调用`toString()`方法，即可从这个Mul类中取出目标代码

```java
public class Mul extends Instruction{	// Instruction继承User
    // 构造方法，记录这条Mul指令的各个要素
    // 包括其名字(例如"%2"),所属基本块，操作数
    public Mul(String name, BasicBlock parent, Value op1, Value op2) {
        super(name, parent, op1, op2);
    }
    // 生成目标代码的字符串
    @Override
    public String toString(){
        return getAresIrString("mul");	// 一个用来计算形如`<result> = add <ty> <op1>, <op2>`的目标代码字符串的方法
    }
}
```

再例如，如下中间代码，将其翻译为Java代码，可以是：

```llvm
%1 = mul i32 1, 2
%2 = mul i32 %1, %1
```

```java
// IrBuilder.buildxxx是封装了具体操作的工厂模式方法，
// 其参数是操作数Value类，返回结果是计算结果Instruction类
Mul mul1 = IrBuilder.buildMulInstruction(new ConstInt(1), new ConstInt(1))
Mul mul2 = IrBuilder.buildMulInstruction(mul2, mul2)    
```

#### 2.5 特殊的Value类：Function和BasicBlock类

`Function`和`BasicBlock`类也可以作为操作数。

例如调用指令：`<result> = call [ret attrs] <ty> <fnptrval>(<function args>)`的实现上，我们直接将function作为了一个操作数，用来构建Call指令。

```java
// 在符号表中找到需要的Function类对象
Function function = (Function) IrSymbolTableStack.getSymbol(identToken.str);
// 实参所对应的Value类对象的列表
ArrayList<Value> argRValues = new ArrayList<>();  
// 使用封装好的方法来构建Call指令
// Irc.synValue全称是IrContext.synthesizeValue
// IrContext里面记录了一些全局变量（综合属性），用来在递归下降中，进行不同下降层级之间的通信
// （例如AddExp可以向下递归到MulExp，根据文法，AddExp依赖于MulExp的计算结果，下层的MulExp可以通过Irc.synxxx来将这个结果传递给上层的AddExp）
Irc.synValue = IrBuilder.buildCallInstruction(function, argRValues, Irc.curBlock);
```

#### 2.6 根节点：Module类

如同编译单元一样的存在

### 3. 关键场合的处理方式

#### 3.1 局部变量的声明与初始化的处理

需要进行的主要操作有：

**`alloca`（分配变量的栈空间，返回指向该空间的指针）**

**`store`（给变量赋值，即向指针所指空间内进行写）**



例如现在需要翻译如下数组定义语句：

```c
int main(){
	int a[3][2] = {{10, 20}, {30, 40}, {50, 60}};	// 全局数组
    return 0;
}
```

**局部变量的分配是在栈上进行的，需要我们手动分配内存**。

可以写出如下llvm语句，来分配（alloca）出这么大的空间。

```llvm
%instr1 = alloca [3 x [2 x i32]]
```

**alloca指令会在指定类型上，套一层指针**，例如此时instr1的类型是**[3 x [2 x i32]]***



接下来，我们需要向分配好的地址中存储常数。

要使用`store`指令给每一个元素赋值，我们就需要获取每一个元素具体地址的指针。

这样，我们就需要用到求地址指令`getelementptr`

```
%instr2 = getelementptr [3 x [2 x i32]], [3 x [2 x i32]]* %instr1, i32 0, i32 0
%instr3 = getelementptr [2 x i32], [2 x i32]* %instr2, i32 0, i32 0
store i32 10, i32* %instr3
%instr5 = getelementptr i32, i32* %instr3, i32 1
store i32 20, i32* %instr5
%instr7 = getelementptr i32, i32* %instr3, i32 2
store i32 30, i32* %instr7
%instr9 = getelementptr i32, i32* %instr3, i32 3
store i32 40, i32* %instr9
%instr11 = getelementptr i32, i32* %instr3, i32 4
store i32 50, i32* %instr11
%instr13 = getelementptr i32, i32* %instr3, i32 5
store i32 60, i32* %instr13
```

> getelementptr求地址指令：
>
> 这个指令可以带一个偏移，或者两个偏移。
>
> 记getelementptr(a, op1)为：对基地址指针a，使用gep指令，带一个操作数（偏移）op1。
>
> 记getelementptr(a, op1, op2)为：对基地址指针a，使用gep指令，带两个操作数（偏移）op1, op2。[要求a一定是指向数组的指针]
>
> 结论为：
>
> - $getelementptr(a, op1) = a + op1 * sizeof(a)$，返回指针类型与a相同。
> - $getelementptr(a, op1) = a + op1 * sizeof(a) + op2 * sizeof(a指向的数组的元素) $【例如如果a指向`a[2][3]`，那么这里`sizeof(a指向的数组的元素)`就是a[0]或者a[1]的大小】，返回指针类型是a“降了一维”后的类型。
>
> 
>
> 以`int a[2][3]`为例，假设a基地址0，容易计算出二维数组a大小为$4*2*3 = 24$，一维数组a[0]大小为$3*4=12$
>
> getelementptr(a, 0)会返回什么？
>
> 会返回0 *24 = 0，即a的地址
>
> getelementptr(a, 1)会返回什么？
>
> 会返回1 * 24 = 1，即飞出去一整个a之后的地址，完全不在a数组之内
>
> getelementptr(a, 0, 0)会返回什么？
>
> 会返回0*24 + 0 * 12 = 0 ，即a[0]的地址
>
> getelementptr(a, 0, 1)会返回什么？
>
> 会返回0*24 + 1 * 12 = 12，即a[1]的地址
>
> getelementptr(a, 666, 233)会返回什么？
>
> 会返回666*24 + 233 * 12，即直接飞出了666个a，再飞出去233个a[0]那么大

