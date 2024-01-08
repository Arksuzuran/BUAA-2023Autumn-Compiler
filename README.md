# 编译文档

该文档为课程最后需要提交的说明文档作业

## 1. 参考编译器介绍

该部分随便水水即可。

![image-20240108224700700](https://arksuzuran.oss-cn-beijing.aliyuncs.com/img/md_img/image-20240108224700700.png)

## 2. 编译器总体设计

#### 2.1 总体结构

编译过程分为如下阶段：

词法分析，语法分析，错误处理，语法分析与`llvm`生成，`llvm`优化，`mips`生成及优化。

- 前端

  - 词法分析。构建了贪心简化的DFA，输入源程序，输出token序列。

  - 语法分析。透过递归下降子程序，实际上进行的是最左推导。其中采用了扩充的BNF范式解决左递归问题，采用了预读FIRST集的方式来解决回溯问题。

  - 错误处理。在词法分析、语法分析中已进行了部分错误处理，这里专门再进行一次递归下降来诊查某些错误。

  - 语法分析与`llvm`代码生成。遍历语法分析生成的AST，构建`llvm`中间代码。构建过程中采用了“走后门”的SSA。

- 中端(`llvm`优化)

  中端实现了Mem2reg，重构并生成了带有`phi`函数的中间代码。

  - 构建控制流图(CFG)，求解支配者与支配边界。
  - 重构SSA，`phi`指令的插入与变量重命名。

- 后端(`mips`代码生成及优化)

  - 非`phi`指令中间代码的翻译，`phi`指令的翻译。至此为止分配的都是虚拟寄存器。
  - 图着色寄存器分配。
  - 少量窥孔优化。


由于在我们的编译实验当中，作业是迭代进行的，因此为了降低开发难度，每个编译阶段都相对独立，且都至少对源程序或其中间形式遍历了一轮，且每一遍中都根据具体需求独立建立一遍符号表。

#### 2.2 文件组织与接口设计

##### 2.2.1 文件组织

各个包及包内的部分关键类罗列如下。

```
├── config		// 参数
│   └── Config.java					// 参数设置
├── frontend	// 前端入口
│   └── Lexer.java					// 词法分析入口及分析方法
|	└── Parser.java					// 语法分析入口及分析方法
|	└── Checker.java				// 错误处理入口
├── token		// token包
│   └── Token.java					// TOken类
|	└── TokenType.java				// Token类型及保留字、分界符等规定信息
├── node		// 用于递归下降的node包
│   └── <文法非终结符>.java			// node类，放置有语法分析、错误处理、ir的递归下降方法	
├── error		// 错误处理
|	└── Error.java					// 错误类
│   └── ErrorHandler.java			// 错误的记录与输出
|	└── ErrorCheckTool.java			// 错误的工厂类
├── symbol		// 错误处理，符号及栈式符号表
│   └── Symbol.java					// 符号类
|	└── SymbolTable.java			// 符号表类
|	└── SymbolTableStack.java		// 栈式符号表类
├── ir			// 中间代码生成与优化
|   ├── analyze				// 中间代码优化
|   ├── types				// value的类型
|   ├── values				// values
|   |	└── constants				// Constant value们
|   │   └── instructions			// Instruction value们
|   |	└── BasicBlock.java			
|   |	└── Function.java			
|   |	└── GlobalVariable.java	
|   |	└── Module.java	
|   |	└── Value.java	
|   |	└── User.java		
|   ├── Irc.java					// 在中间代码生成中保存上下文信息
│   ├── Irbuilder.java				// 中间代码的入口类及工厂类
├── backend		// Mips生成与优化 
|   ├── instructions		// Instruction们
|   ├── operands			// 操作数们，包括立即数，虚拟、物理寄存器，标签
|   ├── opt					// 目标代码优化
|   |	└── BlockliveVarInfo.java	// 活跃变量分析
|   │   └── RegBuilder.java			// 图着色寄存器分配
|   |	└── Peephole.java			// 窥孔优化
|   ├── Mc.java						// 在目标代码生成中保存上下文信息
|   └── MipsBuilder.java			// 目标代码的入口及指令的工厂类
├── utils		// 存放工具类 
└── Compiler.java			// 编译器入口
```

##### 2.2.2 编译器入口

编译器的入口为`src/Compiler.java`。

调用`Compiler`类的`doCompileing()`方法即可对指定文件进行编译，并生成相应的`llvm`和`mips`代码。

##### 2.2.3 参数设定

在`config.Config.java`进行编译器的设置，包括IO路径设置、是否生成`mips`代码、是否开启优化、是否开启控制台debug输出等。例如部分优化配置开关：

```
	// 是否生成Mips 为false则只生成llvm,不进入后端
    public static boolean genMips = true;
// ============== 优化配置开关 ================
    // 是否开启Mem2Reg优化（否则就会生成慢吞吞的半吊子SSA，疯狂偷吃存取指令）
    public static boolean openMem2RegOpt = true;
    // 是否开启寄存器分配（debug用，不开启则会输出虚拟机寄存器）
    public static boolean openRegAllocOpt = true;
```

##### 2.2.4 各编译阶段入口

```
├── frontend
│   └── Lexer.java		// 词法分析
|	└── Parser.java		// 语法分析
|	└── Checker.java	// 错误处理
├── ir
│   ├── Irbuilder.java	// 中间代码生成与优化
└── backend
    └── MipsBuilder.java	// 目标代码生成与优化
```

每一个编译阶段都有其入口类，入口类们在`src/Compiler.java`被串联在一起。

入口类均实现了`CompilePhase`接口。入口类需要在构造方法中传入上一个阶段的输出，再调用`process()`方法以进行分析，最后调用`outputResult()`方法以将结果输出至文件。

## 3. 词法分析

#### 3.1 总体思路

词法分析部分的主要任务是顺序遍历源程序代码，将其按照文法转化为token序列。

词法分析可以透过正则表达式或者**贪心简化过的DFA**实现，我采用了后者。

与词法分析相关的文件结构如下：

```
├── frontend	// 前端入口
│   └── Lexer.java					// 词法分析入口及分析方法
├── token		// token包
│   └── Token.java					// TOken类
|	└── TokenType.java				// Token类型及保留字、分界符等规定信息
```

#### 3.2 DFA的设计

DFA的实现位于`fronted/Lexer.java`

我们的单词大致分为四类，分别是保留字、 字符串常量、标识符、分界符。**透过不同类别的FIRST集合，即可根据当前字符，确定下面将要处理的单词的大致种类**。

![image-20231220103042494](https://arksuzuran.oss-cn-beijing.aliyuncs.com/img/md_img/image-20231220103042494.png)

调用`next()`方法即可读取下一个token，大致框架如下。

```
// 立即获取下一个token
    public Token next () throws LexerException {
        // 跳过注释和空白符
        jumpWhiteCharacter();
        while(jumpNote() != 0){
            jumpWhiteCharacter();
        }
        // 按照首字符进行贪心匹配
        char chr = text.charAt(pos);
        // 1.数字开头 应为数字
        if (Character.isDigit(chr)) {
            ...
        }
        // 2.字母或下划线开头 应为保留字或者标识符
        else if (Character.isLetter(chr) || chr == '_') {
            ...
            TokenType type = TokenType.isReservedToken(str); // 先判断是否是保留字
            // 是保留字
            if(type!=null){
                token = new Token(str, lineNum, type);
            }
            // 是标识符
            else{
                token = new Token(str, lineNum, TokenType.IDENFR);
            }
        }
        // 3.双引号 格式化字符串
        else if(chr == '"'){
            ...
        }
        // 4.单字符或双字符分隔符
        else{
           ...
            if(flagSingle){
                tokenType = TokenType.getTokenType((str = text.substring(pos, pos+1)));
                posMove = 1;
            }
            // 可能的双字符分隔符 要截取两位来生成token
            if(pos < maxPos && flagDouble){
                TokenType tmp = TokenType.getTokenType(text.substring(pos, pos+2));
                // 确认生成成功后，再进行记录，以防覆盖单双皆可的结果
                if(tmp != null){
                    tokenType = tmp;
                    str = text.substring(pos, pos+2);
                    posMove = 2;
                }
            }
			...
        }
		...
    }
```

值得注意的是，还需要对注释进行处理，将单行注释与多行注释过滤掉，仅输出有效语法成分。

#### 3.3 Token类

##### 3.3.1 token

记录了Token的原字符串，token的类型，以及其所在行号（供错误处理使用）

```
// token/Token.java
public class Token {
    public String str; // 读入的字符串
    public int lineNum;
    public TokenType type;
	...
}
```

##### 3.3.2 文法设定的保存

位于`token/TokenType.java`

我对于Token的存储方式是一符一类，定义在枚举类`TokenType`里。

此外，为了便于状态机读取当前字符后的进一步状态转移，我们还需要知道文法中规定的保留字（关键字表）、单/双分界符都有哪些。

以上两种信息我都认为是“文法给出的设定”，因此在设计上理应放入一个类中，这使得我们的`Lexer`类并不会把文法写死在代码里，便于后续期中期末考试修改文法时，快速完成词法分析部分的修改。

举例来说，我们如果要增加switch关键字，那么应该在`TokenType.java`里做出如下改动：

```
// token/TokenType.java
// 新增Token类型
SWITCHTK("switch"),
// 加入保留字列表
// 保留字列表
    public static final List<String> reservedTokenList = Arrays.asList(
            "switch", ...);
```

## 4. 语法分析

#### 4.1 总体思路

语法分析的任务是是读入token序列，并分析确定其语法结构，最终生成语法树的过程。

我使用了递归下降分析法来实现语法分析：为每个非终结符编写一个递归子程序，以完成该非终结符所对应的语法成分的分析与识别任务，若正确识别，则可以退出该非终结符号的子程序，返回到上一级的子程序继续分析；若发生错误，即源程序不符合文法，则要进行相应的错误信息报告以及错误处理。

递归下降子程序本质上是在进行最左推导，可能会遇到左递归和回溯的问题。

#### 4.2 递归下降的实现

递归下降的实现位于`frontend/Parser.java`

具体来说，有两个设计。

##### 4.2.1 `nextsym()`的封装: `matchToken()`

我们知道，语法分析的推进，本质上是终结符识别进度的推进，因此**`nextsym`的调用时机应当是成功识别一个终结符后**。既然如此，我们为何不**将终结符识别、`nextsym`（以及后续的错误处理）都封装在一个函数里**呢？

```java
// 所有匹配最终都会归结到对终结符的匹配上，而取下一个终结符的时机就是上一个终结符被读取时，因此将这两个操作合二为一：
    // 将当前token与指定类型的token相匹配，匹配成功则推进匹配进度pos++，否则进行报错
    // 对于规定的若干错误类型，不予报错，而是记录并进行补全
    private Token matchToken(TokenType tokenType) {
        if (curToken.type == tokenType) {
            Token tmp = curToken;
            if (pos < maxPos) {
                pos++;
                curToken = tokens.get(pos);
            }
            return tmp;
        }
        // 匹配失败 尝试匹配错误类型 此处curToken不必再向下滑动
        ...
        return null;
    }
```

如此一来，**当我们要接纳（注意不是预读）一个语法成分时，我们可以理所当然地调用该方法**。如果成功，那么向下读取下一个字符串，如果失败则不会读取下一个字符（并尝试进行错误处理）。

这样还可以保证，**任何时候，当前的`curToken`一定是最新的未接纳的字符**。

##### 4.2.2 子程序: `<非终结符>()`

每一个非终结符都有其递归下降子程序。递归下降子程序的返回值是解析好的语法树结点`node`类。

递归下降子程序主要进行如下工作：

- 根据FIRST集或者回溯，来选择合适的产生式。
- **根据产生式依序接纳终结符：调用`matchToken()`**
- **根据产生式依序进行右端非终结符的进一步递归下降分析并接纳：调用相应子程序。**
- 最后构建语法树结点`node`类，供上一层的子程序取用。

以`ConstInitVal`的子程序为例：

```java
// ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    // 后者的first是 LBRACE, 以此进行区分
    private ConstInitValNode ConstInitVal() {
        ConstExpNode constExpNode = null;
        Token lbraceToken = null;
        ArrayList<ConstInitValNode> constInitValNodes = new ArrayList<>();
        ArrayList<Token> commaTokens = new ArrayList<>();
        Token rbraceToken = null;
        // 数组的初始化, 以{开头
        if (curToken.type == TokenType.LBRACE) {
            lbraceToken = matchToken(TokenType.LBRACE);
            // 下一个token不是右括号, 那么中间应当含有constinitval
            if (curToken.type != TokenType.RBRACE) {
                constInitValNodes.add(ConstInitVal());
                while (curToken.type == TokenType.COMMA) {
                    commaTokens.add(matchToken(TokenType.COMMA));
                    constInitValNodes.add(ConstInitVal());
                }
            }
            // 无论中间是否有数组初始值，都应该匹配右括号
            rbraceToken = matchToken(TokenType.RBRACE);
        }
        // 非数组初始化，仅有一个constexp
        else {
            constExpNode = ConstExp();
        }
        return new ConstInitValNode(constExpNode, lbraceToken, constInitValNodes, commaTokens, rbraceToken);
    }
```

##### 4.2.3 语法树的结点:`node`类

位于`node`包下。

**`node`类是语法分析中，语法树的结点，记录了语法分析的结果**。是贯穿了语法分析、错误处理、中间代码生成的重要类。

对于每一个非终结符，都建立一个`node`类。所有`node`类都继承了抽象父类`Node`。

`Node`规定了所有`node`类都要实现的三个方法，用于**在遍历语法树时执行相应的动作**（即递归下降时的动作符号）：

- `print()`方法，透过遍历语法树，输出语法分析结果至文件。
- `check()`方法，透过遍历语法树，检查错误并记录。
- `buildIr()`方法，透过遍历语法树，生成`llvm`代码。

```java
public class ConstInitValNode extends Node{
    // 语法树结点记录字段
    private ConstExpNode constExpNode;
    private Token lbraceToken;
    // 需注意，constInitValNodes比 commaTokens长1
    private ArrayList<ConstInitValNode> constInitValNodes;
    private ArrayList<Token> commaTokens;
    private Token rbraceToken;
	
    // 
    public void setDims(ArrayList<Integer> dims) {
        this.dims = dims;
    }
    /**
     * 各维的长度
     */
    private ArrayList<Integer> dims;
	// 构造方法
    public ConstInitValNode(...) {
        ...
    }
	// 输出语法分析结果至文件
    @Override
    public void print() {
        if(lbraceToken != null){
            lbraceToken.print();
            if(!constInitValNodes.isEmpty()){
                constInitValNodes.get(0).print();
                if(!commaTokens.isEmpty()){
                    for(int i=0; i<commaTokens.size(); i++){
                        commaTokens.get(i).print();
                        constInitValNodes.get(i+1).print();
                    }
                }
            }
            rbraceToken.print();
        }
        else if(constExpNode != null){
            constExpNode.print();
        }
        printNodeType();
    }
	// 错误处理
    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    @Override
    public void check() {
        if(lbraceToken != null){
            if(!constInitValNodes.isEmpty()){
                for(ConstInitValNode constInitValNode : constInitValNodes){
                    constInitValNode.check();
                }
            }
        }
        else if(constExpNode != null){
            constExpNode.check();
        }
    }
    // 中间代码生成
    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    /**
     * 向上传递常量的初始值
     * @synValue 解析好的初值，ConstInt 或 ConstArray
     * @synValueArray    解析好的展平Value列表   ArrayList<Value>
     */
    @Override
    public void buildIr() {
        // 直接向上传递结果
        if(constExpNode != null){
            constExpNode.buildIr();
        }
        // 需要向下迭代 ConstInitVal, ConstInitVal, ConstInitVal...
        else{
            // 全局常量数组
            // 需要构建constantInitArray，传递给synValue,用于常量数组的初始化
            // 局部常量数组
            // 需要构建constantInitArray，传递给synValue,用于常量数组的初始化
            // 需要构建展平的Value数组，传递给synValueArray，用于store常量数组的值
            ArrayList<Constant> constantInitArray = new ArrayList<>();
            ArrayList<Value> flattenValueArray = new ArrayList<>();
            // 一维，说明下层constExpNode != null 且传上来的是ConstInt
            if(dims.size() == 1){
                for(ConstInitValNode constInitValNode : constInitValNodes){
                    constInitValNode.buildIr();
                    constantInitArray.add((ConstInt) Irc.synValue);
                    flattenValueArray.add(Irc.synValue);
                }
            }
            // 多维，还需要为下层设置dim 因为下层会进入上边一维的分支，即用到dims
            // 且传上来的是ConstArray
            else{
                for(ConstInitValNode constInitValNode : constInitValNodes){
                    constInitValNode.setDims(new ArrayList<>(dims.subList(1, dims.size())));
                    constInitValNode.buildIr();
                    constantInitArray.add((ConstArray) Irc.synValue);
                    flattenValueArray.addAll(Irc.synValueArray);
                }
            }
            Irc.synValue = new ConstArray(constantInitArray);
            Irc.synValueArray = flattenValueArray;
        }
    }
}
```

**这里很可惜的一点是，其实不必记录终结符。因为终结符只在输出语法树时才有用，而在后面的中间代码生成中时，只有非终结符结点才能提供有效信息。**等意识到这一点时，已经完成了语法分析和错误处理，没有进行重构。

##### 4.2.4 语法树的打印:`print()`

位于`node`包的各个`node`类下。

`print()`方法的含义是，以当前结点为根节点，遍历这个子树并输出其语法分析结果到文件。

**我们对于语法树的打印，本质上仍然是对于终结符们的打印**，因此`token`类亦添加了`print()`方法。

示例详见上一小节的`print()`方法，直接无脑对于所有成分进行`print`即可。

#### 4.3 左递归的处理

涉及到左递归的文法主要是关于Exp系列的非终结符。

```
AddExp -> MulExp | AddExp ('+' | '−') MulExp
```

##### 4.3.1 右递归改写

一个处理方式是将左递归改为右递归，例如：

```
AddExp -> MulExp | MulExp ('+' | '−') AddExp
```

![image-20231220110847759](https://arksuzuran.oss-cn-beijing.aliyuncs.com/img/md_img/image-20231220110847759.png)

但如此处理是存在潜在风险的，因为这修改了文法。

##### 4.3.2 扩充的BNF范式

还有一种处理方式是采用BNF范式，修改了文法，直接丢掉`AddExp`。

```
AddExp -> MulExp {'+' MulExp}
```

![image-20231220111136501](https://arksuzuran.oss-cn-beijing.aliyuncs.com/img/md_img/image-20231220111136501.png)

这种处理方式依然存在风险，因为后续的所有编译环节都要相应地使用修改后的文法。

##### 4.3.3 我的处理

我的办法是**在扩充的BNF范式的基础上再进一步，把刚刚拆出来的所有`MulExp`全部手动组装回左递归的形态**。左递归对于后续的编译部分不再有影响，因为我们的信息已经全部存储在编译器中，不再受到最左推导的限制。

```
// 加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp
    private AddExpNode AddExp() {
        MulExpNode mulExpNode = MulExp();
        Token opToken = null;
        AddExpNode addExpNode = null;

        // 存在('+' | '−') 那么捕获外层结构，然后组装回一层层的AddExp
        while(curToken.type == TokenType.PLUS || curToken.type == TokenType.MINU){
            // 将上一轮捕获的单位进行组装
            addExpNode = new AddExpNode(mulExpNode, opToken, addExpNode);
            // 以('+' | '−') MulExp为单位继续进行捕获
            opToken = matchToken(curToken.type);
            mulExpNode = MulExp();
        }
        return new AddExpNode(mulExpNode, opToken, addExpNode);
    }
```

具体来说，先识别一个必有的`MulExp`，随后每一次都识别一个`+MulExp`。**每识别到一个`+MulExp`，我们便将上一轮识别到的`AddExp`和上一轮识别到的`+MulExp`组装成本轮的新`AddExp`**，这样就可以在不破坏文法的情况下，成功识别到该有的语法结构。

#### 4.4 回溯的处理

对于一般的产生式，我们采用FIRST集来判别即可，但是也有一些不那么友好的产生式，我们难以判别：

- 选择哪个产生式。
- 产生式中`{xxx}`的重复部分，到底应该何时结束。

**重复部分啥时候结束？看看`)`，`]`不就好了？事实上，我们在后续错误处理中采取的，补全的错误局部化策略，迫使我们的程序在`)`，`]`缺失时依然要正常工作。为解决这个问题，我们只得以循环部的FIRST集来判别**。

回到我们的回溯问题来。

**回溯，即FIRST也失灵的情况下，我们被迫跳过某个非终结符，对后面的标志性符号进行分析，从而判断选取的产生式分支的手段。**

回溯只产生在`Stmt`的左值与`Exp`判别上：

```
Stmt -> LVal '=' Exp ';'
Stmt -> [Exp] ';'
Stmt -> LVal '=' 'getint''('')'';'
```

在这里，`Exp`当然可以透过`PrimaryExp`推出左值`LVal`，FIRST集失灵。

为此，我们要处理掉碍事的`LVal`，暴露出后面的`=`和`getint`。

回溯主要是采用`savePos()`和`recoverPos()`来记录、复原当前游标在token序列的位置。详细代码及注释如下：

```java
private StmtNode Stmt() {
        ArrayList<Token> tokens = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        StmtNode.StmtType type;
        ArrayList<Boolean> posFlag = new ArrayList<>(Arrays.asList(false, false, false));
        switch (curToken.type) {
            case xxx ->{
                ...
            }
            default -> {
                // 余下三种情况：
                //  LVal '=' Exp ';'
                //  [Exp] ';'
                //  LVal '=' 'getint''('')'';'
                // 其中：左值表达式 LVal → Ident {'[' Exp ']'}
                //      表达式 Exp → AddExp → MulExp → UnaryExp  → PrimaryExp | Ident   PrimaryExp → '('
                // 直接为分号，则无需进行任何操作
                if (curToken.type == TokenType.SEMICN) {
                    tokens.add(matchToken(TokenType.SEMICN));
                    type = StmtNode.StmtType.EXP;
                }
                else{
                    // 先使用Exp消去LVal和Exp
                    savePos();
//                    System.out.println("stmt开始进行试探性检验" + curToken);
                    ExpNode expNode = Exp();
//                    System.out.println("stmt检验完成" + curToken);
                    // 直接为分号，应该是Exp
                    if (curToken.type == TokenType.SEMICN) {
                        nodes.add(expNode);
                        tokens.add(matchToken(TokenType.SEMICN));
                        type = StmtNode.StmtType.EXP;
                    }
                    // 非分号，那么进行回溯以读取LVal
                    else {
                        recoverPos();
                        LValNode lValNode = LVal();
                        nodes.add(lValNode);
                        tokens.add(matchToken(TokenType.ASSIGN));
                        // LVal '=' 'getint''('')'';'
                        if (curToken.type == TokenType.GETINTTK) {
                            tokens.add(matchToken(TokenType.GETINTTK));
                            tokens.add(matchToken(TokenType.LPARENT));
                            tokens.add(matchToken(TokenType.RPARENT));
                            tokens.add(matchToken(TokenType.SEMICN));
                            type = StmtNode.StmtType.LVALGETINT;
                        }
                        // LVal '=' Exp ';'
                        else {
                            nodes.add(Exp());
                            tokens.add(matchToken(TokenType.SEMICN));
                            type = StmtNode.StmtType.LVALASSIGN;
                        }
                    }
                }
            }
        }
        return new StmtNode(type, tokens, nodes, posFlag);
    }
```

## 5. 错误处理

错误处理的作用是诊察出指定的词法、语法和语义错误， 同时要求进行错误局部化处理，并输出错误信息。错误的类型如下：

| 错误类型                             | 错误类别码 | 解释                                                         | 对应文法及出错符号 ( … 表示省略该条规则后续部分)             |
| ------------------------------------ | ---------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 非法符号                             | a          | 格式字符串中出现非法字符报错行号为 **<FormatString>** 所在行数。 | <FormatString> → ‘“‘{<Char>}’”                               |
| 名字重定义                           | b          | 函数名或者变量名在**当前作用域**下重复定义。注意，变量一定是同一级作用域下才会判定出错，不同级作用域下，内层会覆盖外层定义。报错行号为 **<Ident>** 所在行数。 | <ConstDef>→<Ident> … <VarDef>→<Ident> … <Ident> … <FuncDef>→<FuncType><Ident> … <FuncFParam> → <BType> <Ident> … |
| 未定义的名字                         | c          | 使用了未定义的标识符报错行号为 **<Ident>** 所在行数。        | <LVal>→<Ident> … <UnaryExp>→<Ident> …                        |
| 函数参数个数不匹配                   | d          | 函数调用语句中，参数个数与函数定义中的参数个数不匹配。报错行号为函数调用语句的**函数名**所在行数。 | <UnaryExp>→<Ident>‘(’[<FuncRParams>]‘)’                      |
| 函数参数类型不匹配                   | e          | 函数调用语句中，参数类型与函数定义中对应位置的参数类型不匹配。报错行号为函数调用语句的**函数名**所在行数。 | <UnaryExp>→<Ident>‘(’[<FuncRParams>]‘)’                      |
| 无返回值的函数存在不匹配的return语句 | f          | 报错行号为 **‘return’** 所在行号。                           | <Stmt>→‘return’ {‘[’<Exp>’]’}‘;’                             |
| 有返回值的函数缺少return语句         | g          | 只需要考虑函数末尾是否存在return语句，**无需考虑数据流**。报错行号为函数**结尾的’}’** 所在行号。 | <FuncDef> → <FuncType> <Ident> ‘(’ [<FuncFParams>] ‘)’ <Block> <MainFuncDef> → ‘int’ ‘main’ ‘(’ ‘)’ <Block> |
| 不能改变常量的值                     | h          | <LVal>为常量时，不能对其修改。报错行号为 **<LVal>** 所在行号。 | <Stmt>→<LVal>‘=’ <Exp>‘;’ <Stmt>→<LVal>‘=’ ‘getint’ ‘(’ ‘)’ ‘;’ |
| 缺少分号                             | i          | 报错行号为分号**前一个非终结符**所在行号。                   | <Stmt>,<ConstDecl>及<VarDecl>中的’;’                         |
| 缺少右小括号’)’                      | j          | 报错行号为右小括号**前一个非终结符**所在行号。               | 函数调用(<UnaryExp>)、函数定义(<FuncDef>)及<Stmt>中的’)’     |
| 缺少右中括号’]’                      | k          | 报错行号为右中括号**前一个非终结符**所在行号。               | 数组定义(<ConstDef>,<VarDef>,<FuncFParam>)和使用(<LVal>)中的’]’ |
| printf中格式字符与表达式个数不匹配   | l          | 报错行号为 **‘printf’** 所在行号。                           | <Stmt> →‘printf’‘(’<FormatString>{,<Exp>}’)’‘;’              |
| 在非循环块中使用break和continue语句  | m          | 报错行号为 **‘break’** 与 **’continue’** 所在行号。          | <Stmt>→‘break’‘;’ <Stmt>→‘continue’‘;’                       |

错误处理的各个错误类型是相对独立的，彼此之间仅存在一定的联系，可以分别对每一种错误进行处理。在进行错误处理的过程中，我建立了**栈式符号表**。



错误处理相关的项目文件罗列如下：

```
├── frontend	// 前端入口
|	└── Checker.java				// 错误处理入口
├── node		// 用于递归下降的node包
│   └── <文法非终结符>.java			// node类，放置有语法分析、错误处理、ir的递归下降方法	
├── error		// 错误处理
|	└── Error.java					// 错误类
│   └── ErrorHandler.java			// 错误的记录与输出
|	└── ErrorCheckTool.java			// 错误的工厂类
├── symbol		// 错误处理，符号及栈式符号表
│   └── Symbol.java					// 符号类
|	└── SymbolTable.java			// 符号表类
|	└── SymbolTableStack.java		// 栈式符号表类
```

#### 5.1 符号表

##### 5.1.1 符号Symbol

我将符号的类型分为函数符号`FuncSymbol`和变量符号`NumSymbol`，二者都继承一个父类`Symbol`。

对于函数符号，我们额外记录其返回值类型以及形参的Symbol：

```java
public class FuncSymbol extends Symbol{
    public enum FuncReturnType{
        INT, VOID;
    }
    private FuncReturnType returnType;
    private ArrayList<NumSymbol> params;    // 参数符号列表
    ...
}
```

对于变量符号，由于其只可能是int或者int数组类型，因此我们只需要额外记录其维数。

```java
public class NumSymbol extends Symbol{
    private int dim;        // 数组维数 0 1 2
    ...
}
```

##### 5.1.2 符号表SymbolTable

对于符号表，我使用**栈式符号表**。在设计之初，我是想要保留一个双向的类似树结构（其实也确实实现了，**但是ir生成时没有采用该符号表**），在使用栈式符号表后保留根节点，这样就可以留下该符号表。具体来说是把下图中的单向边换成双向边：

![image-20231220163717060](https://arksuzuran.oss-cn-beijing.aliyuncs.com/img/md_img/image-20231220163717060.png)

每个符号表中，都由`TreeMap`来存储具体信息，`fatherSymbolTable`和`sonSymbolTables`维护父子间的关系。

```java
public class SymbolTable {
    private final TreeMap<String, Symbol> symbolMap;		// 符号表
    public final SymbolTable fatherSymbolTable;    			// 所属的父符号表
    private final ArrayList<SymbolTable> sonSymbolTables;   // 所有儿子符号表
    private final Node node;    							// 绑定的node
    public SymbolTable(SymbolTable fatherSymbolTable, Node node) {
        ...
    }
    ...
}
```

##### 5.1.3 符号表栈SymbolTableStack

该类内封装有构建符号表、查符号表的各种方法，以及一个符号表栈`Stack`。

该类内还有为`continue`，`break`，`return`等对应错误而设置的方法。

```java
public class SymbolTableStack {
    private final Stack<SymbolTable> stack;   //
    // ====================== 栈操作=======================
    // 创建新的符号表并入栈
    public static void push(SymbolTable symbolTable){
        ...
    }
    // 创建新的node对应的符号表并入栈
    public static void push(Node node){
        push(new SymbolTable(peek(), node));
    }
    // 将栈顶符号表出栈
    public static void pop(){
        instance.stack.pop();
    }
    // 访问栈顶元素 如果栈为空那么返回null
    public static SymbolTable peek(){
        ...
    }
    // 向栈顶符号表中添加元素
    public static void addSymbolToPeek(Symbol symbol){
        ...
    }
    // ================= 栈查找 =================
    // 检测栈顶符号表是否包含指定名称的元素
    public static boolean peekHasSymbol(String name){
        ...
    }
    // 检查整个栈内是否包含指定名称的元素
    public static boolean stackHasSymbol(String name){
        ...
    }
    // 检查整个栈<第一个>指定名称的元素 是否为指定类型。如果没有该元素，也返回false
    public static boolean stackHasSymbol(String name, SymbolType symbolType){
        ...
    }
    // 在整个栈内查找并返回第一个指定名称指定类型的元素
    public static Symbol getSymbol(String name, SymbolType symbolType){
        ...
    }
	// ================= 循环的处理 =================
    // 记录当前所在的循环深度
    private int circleDepth = 0;
    // 进入循环相关
    public static boolean inLoop() {
        return instance.circleDepth > 0;
    }
    public static void enterLoop(boolean into) {
        ...
    }
    // ================= 函数的处理 =================
    // 记录当前是否在一个无返回值的函数内部
    private boolean inVoidFunc = false;
    // 进入函数定义相关
    public static boolean inVoidFunc() {
        return instance.inVoidFunc;
    }
    public static void setInVoidFunc(boolean inVoidFunc) {
        instance.inVoidFunc = inVoidFunc;
    }
}
```

##### 5.1.4 入栈新符号表的时机

在遍历语法树的过程中，于`MainFuncDef`，`FuncDef`以及`Stmt`的`StmtType.BLOCK`场合下，调用`SymbolTableStack`中封装好的方法即可入栈新的符号表，例如：

```java
// StmtNode.java
	// 如果是要进入Block 那么应当入栈新符号表
        if(type == StmtType.BLOCK){ SymbolTableStack.push(this); }
// SymbolTableStack.java
	// 创建新的符号表并入栈
    public static void push(SymbolTable symbolTable){
        // 栈非空，则栈顶元素应当记录当前进入的符号表为son
        if(!instance.stack.empty()){
            peek().addSon(symbolTable);
        }
        instance.stack.push(symbolTable);
    }
    // 创建新的node对应的符号表并入栈
    public static void push(Node node){
        push(new SymbolTable(peek(), node));
    }
```

##### 5.1.5 添加新符号的时机

在遍历语法树的过程中，于`MainFuncDef`，`FuncDef`以及`Stmt`的`StmtType.BLOCK`场合下，读取到新符号（变量、常量、函数）时调用`SymbolTableStack`中封装好的方法即可。

```java
// FuncDefNode.java
	// 构建该函数的符号
    FuncSymbol funcSymbol = new FuncSymbol(identToken.str, identToken.lineNum, this, returnType, params);
    SymbolTableStack.addSymbolToPeek(funcSymbol);
// SymbolTableStack.java
	// 向栈顶符号表中添加元素
    public static void addSymbolToPeek(Symbol symbol){
        peek().addSymbol(symbol);
    }
```

#### 5.2 错误处理的逻辑

##### 5.2.1 错误记录方法

在`error/ErrorCheckTool.java`内封装有检查当前语境下错误并记录的方法。在遍历到语法树的特定位置时直接调用，即可进行错误处理。

##### 5.2.1 记录错误的接口

我将所有能够生成并记录错误的方法都统一放置在了`error/ErrorCheckTool.java`里，调用其中的方法便可（判断并）记录错误，便于后期修改。例如：

```java
// 检测并处理重定义问题 返回其是否未出现重定义问题
    // false: 出现问题
    // true: 未出现问题
    public static boolean judgeAndHandleDuplicateError(Token token){
        // 在栈顶的符号表中检测是否存在重复定义
        if(SymbolTableStack.peekHasSymbol(token.str)){
            ErrorHandler.addError(new Error(ErrorType.b, token.lineNum));
            return false;
        }
        return true;
    }
```

##### 5.2.2 具体错误的处理方法

- **a - 非法符号；l - 字符串`%d`参数不对应**

  判断字符串当中是否含有非法字符结构，在遍历到`formatStringToken`时处理即可。

  ```java
  // StmtNode.java
  // 'printf''('FormatString{,Exp}')'';'
              case PRINTF -> {
                  Token formatStringToken = getFormatStringToken();
                  // 检查字符串本身是否合法
                  if(!checkFormatString(formatStringToken.str)){
                      ErrorHandler.addError(new Error(ErrorType.a, formatStringToken.lineNum));
                  }
                  // 检查%d和实际参数的个数是否对应
                  if(getFormatStringDNum(formatStringToken.str) != nodes.size()){
                      ErrorHandler.addError(new Error(ErrorType.l, formatStringToken.lineNum));
                  }
              }
  ```

- **b 类错误 - 名字重定义**

  对于题目给出的四条文法，读取到`ident`的创建时，调用：

  时间不是很够了，文档写不完了，截个图吧()

  ![image-20231220170649806](https://arksuzuran.oss-cn-beijing.aliyuncs.com/img/md_img/image-20231220170649806.png)

  ```java
  // 检测并处理重定义问题 返回其是否未出现重定义问题
      // false: 出现问题
      // true: 未出现问题
      public static boolean judgeAndHandleDuplicateError(Token token){
          // 在栈顶的符号表中检测是否存在重复定义
          if(SymbolTableStack.peekHasSymbol(token.str)){
              ErrorHandler.addError(new Error(ErrorType.b, token.lineNum));
              return false;
          }
          return true;
      }
  ```

- **c 类错误 - 未定义的名字**

  对于题目给出的三条文法，读取到`ident`的引用时，调用：

  时间不是很够了，文档写不完了，截个图吧()

  ![image-20231220170735682](https://arksuzuran.oss-cn-beijing.aliyuncs.com/img/md_img/image-20231220170735682.png)

  ```java
  // 检测并处理无定义问题 返回其是否未出现无定义问题
      // false: 出现问题
      // true: 未出现问题
      public static boolean judgeAndHandleUndefinedError(Token token){
          // 在栈顶的符号表中检测是否存在定义
          if(!SymbolTableStack.stackHasSymbol(token.str)){
              ErrorHandler.addError(new Error(ErrorType.c, token.lineNum));
              return false;
          }
          return true;
      }
  ```

- **d 类错误 - 函数参数个数不匹配**

  对于题目给出的文法，当`UnaryExp`需要进行函数调用时，从符号表得到其形参个数，再读取其子节点的`funcRParamsNode`的`expNodes`的`size()`，两者比较，如果不同，那就报错。

  ```java
  // 先检查符号未定义c
              if(ErrorCheckTool.judgeAndHandleUndefinedError(identToken)){
                  // 检查其是否为函数符号 如果不是 则报参数个数错误
                  FuncSymbol symbol = (FuncSymbol) SymbolTableStack.getSymbol(identToken.str, SymbolType.Function);
                  if(symbol == null){
                      ErrorHandler.addError(new Error(ErrorType.d, identToken.lineNum));
                  }
                  // 检测参数个数是否一致
                  else if(symbol.getParams().size() != getParamsNum()){
                      ErrorHandler.addError(new Error(ErrorType.d, identToken.lineNum));
                  }
                  // 检验参数类型是否一一对应
                  else if(!checkParamsSame(symbol)){
                      ErrorHandler.addError(new Error(ErrorType.e, identToken.lineNum));
                  }
              }
  ```

- **e 类错误 - 函数参数类型不匹配**

  我们的变量类型只有三种：整型、一维数组、二维数组。

  ```java
  // 检验参数类型是否一致
      private boolean checkParamsSame(FuncSymbol symbol){
          if(funcRParamsNode == null){
              return symbol.getParams().size() == 0;
          } else {
              ArrayList<NumSymbol> fParams = symbol.getParams();
              ArrayList<ExpNode> expNodes = funcRParamsNode.getExpNodes();
              int fParamDim, rParamDim;
              for(int i = 0; i < symbol.getParams().size(); i++){
                  fParamDim = fParams.get(i).getDim();    // 形参维数
                  rParamDim = expNodes.get(i).getDim();   // 实参维数
                  // -2代表维数不确定（即实参未定义），此处不处理，留到后面报出错误c
                  if(rParamDim != -2 && fParamDim != rParamDim){
                      return false;
                  }
              }
              return true;
          }
      }
  ```

  对于函数形参，我们已经在创建函数符号时记录了其维度信息，直接取用，

  对于函数实参，需要现查维数，即从`ExpNode`开始一个小小的递归查找。

  能够贡献出维数信息的结点主要是：

  - `UnaryExpNode`：UnaryExp → Ident '(' [FuncRParams] ')'。直接采取函数返回值
  - `PrimaryExp `Node：PrimaryExp → Number。Number的维数是0

  - `LValNode`：LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组。根据符号表中查得的`Ident`维数，减去数组取值的维数即可。

- **f 类错误 - 无返回值的函数存在不匹配的`return`语句；g 类错误 - 有返回值的函数缺少`return`语句**

  在`stmt`的类型是`Return`时，如果在函数当中但`expNode != null`就报错。

- **h 类错误 - 不能改变常量的值**

  判断`<LVal>`当中的`<ident>`是否是常量即可。

- **i j k 类错误 - 缺少分号、缺少右小括号’)’、缺少右中括号’]’**

  在语法分析部分的`matchToken()`函数处理，并进行补全的错误局部化处理即可。

  ```java
  private Token matchToken(TokenType tokenType) {
          if (curToken.type == tokenType) {
              ...
          }
          // 匹配失败 尝试匹配错误类型 此处curToken不必再向下滑动
          else if(tokenType == TokenType.SEMICN || tokenType == TokenType.RPARENT || tokenType == TokenType.RBRACK){
              // 此处应该返回前一个非终结符的位置 这里前一个非终结符一定不为空
              int lineNum = tokens.get(pos-1).lineNum;
              String str = tokenType.getStr();
              ErrorType errorType = tokenErrorMap.get(tokenType);
              ErrorHandler.addError(new Error(errorType, lineNum));
              return new Token(str, lineNum, tokenType);
          }
          ...
      }
  ```

- **m 类错误 - 在非循环块中使用 `break` 和 `continue` 语句**

  我们在符号表栈内维护了`loopCount`，初始为 0，每次进入循环就加一，退出循环就减一。

  在 `stmt` 的类型是 `Break` 或者 `Continue` 时，判断当前的 `loopCount` 是否为零即可，如果是，那就报错。

## 6. 中间代码生成与优化

LLVM是一种三地址码，即一条LLVM语句可以表示为如下形式：

```llvm
<运算结果> = <指令类型> <操作数1>, <操作数2>
```

观察这种指令可以发现，一条语句主要由三个要素组成：

（1）操作数（2）指令类型（3）运算结果

现在**我们需要将LLVM的这种语言特性，使用Java的类设计来表达**：

1. **在Java代码中，使用具体的类对象，来表示语句中的各个元素**。（就像语法分析中使用各种`node`类来表达各种语法元素一样）
2. 具体措施是，**通过遍历之前语法分析的语法树，透过结合属性翻译文法的递归下降的方式，来生成llvm的语法树**。

#### 6.1 对LLVM的简单理解

我们将`llvm`采用树的形式存储，根节点为`Module`。

- 一个 `module` 中可以拥有两种顶层实体： `Function` 和 `GlobalVariable`
- 每个 `Function` 下都有若干基本块 `BasicBlock`
- 每个 `BasicBlock`下都有若干指令 `instruction`

在以上结构中，关键节点类的设计如下：

![image20231110104112393](https://arksuzuran.oss-cn-beijing.aliyuncs.com/img/md_img/image-20231110104112393.png)

**空心粗箭头表示类继承关系**（User和Value类也是继承关系，图应该是画错了）。

**实心细箭头表示聚合关系，从而形成树结构**（例如每一个基本块`BasicBlock`里有一条条指令`Instruction`）。

```llvm
<运算结果> = <指令类型> <操作数1>, <操作数2>
```

##### 6.1.1 操作数 Value类

将操作数表示为一个类：Value，它**表示能够作为操作数的对象**。

例如如下乘法语句中：

```
%2 = mul i32 %1, 2
```

按照上面的设计，**`%1`和`2`都是Value，在Java代码中都以Value类的形式存在**。

##### 6.1.2 操作数使用者 User类

将指令的运算结果表示为一个类：User，它**表示能够作为运算结果的对象，或者说是操作数的使用者**。

例如如下乘法语句中：

```
%2 = mul i32 %1, 2
```

按照上面的设计，`%2`是User，它`use`了`%1`和`2`。**`%2`在Java代码中以User类的形式存在**。

##### 6.1.3 User类继承Value类

观察如下语句：

```
%2 = mul i32 %1, 2	
%3 = add i32 %2, 3	
```

乘法产生的`%2`**运算结果**在下一条加法语句中**作为了操作数**。`User`类应该继承`Value`类，这样后面的运算才能用到前面的结果。

##### 6.1.4 指令 Instruction类

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
// IrBuilder.buildxxx是封装了具体操作的工厂模式方法，
// 其参数是操作数Value类，返回结果是计算结果Instruction类
Mul mul1 = IrBuilder.buildMulInstruction(new ConstInt(1), new ConstInt(1))
Mul mul2 = IrBuilder.buildMulInstruction(mul2, mul2)    
```

常用的llvm指令罗列如下：

| llvm ir       | usage                                                        | intro                                       |
| ------------- | ------------------------------------------------------------ | ------------------------------------------- |
| add           | `<result> = add <ty> <op1>, <op2>`                           | /                                           |
| sub           | `<result> = sub <ty> <op1>, <op2>`                           | /                                           |
| mul           | `<result> = mul <ty> <op1>, <op2>`                           | /                                           |
| sdiv          | `<result> = sdiv <ty> <op1>, <op2>`                          | 有符号除法                                  |
| icmp          | `<result> = icmp <cond> <ty> <op1>, <op2>`                   | 比较指令                                    |
| and           | `<result> = and <ty> <op1>, <op2>`                           | 与                                          |
| or            | `<result> = or <ty> <op1>, <op2>`                            | 或                                          |
| call          | `<result> = call [ret attrs] <ty> <fnptrval>(<function args>)` | 函数调用                                    |
| alloca        | `<result> = alloca <type>`                                   | 分配内存                                    |
| load          | `<result> = load <ty>, <ty>* <pointer>`                      | 读取内存                                    |
| store         | `store <ty> <value>, <ty>* <pointer>`                        | 写内存                                      |
| getelementptr | `<result> = getelementptr <ty>, * {, [inrange] <ty> <idx>}*` `<result> = getelementptr inbounds <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*` | 计算目标元素的位置（仅计算）                |
| phi           | `<result> = phi [fast-math-flags] <ty> [ <val0>, <label0>], ...` | 在 SSA 时需要考虑的                         |
| zext..to      | `<result> = zext <ty> <value> to <ty2>`                      | 类型转换，将 `ty`的`value`的type转换为`ty2` |

| llvm ir | usage                                                        | intro                          |
| ------- | ------------------------------------------------------------ | ------------------------------ |
| br      | `br i1 <cond>, label <iftrue>, label <iffalse>` `br label <dest>` | 改变控制流                     |
| ret     | `ret <type> <value>` ,`ret void`                             | 退出当前函数，并返回值（可选） |

我在 LLVM IR 当中，给每条指令都写了一个类（Binary 指的是诸如 add sub mul 这样的二元指令），为了方便我对每种类型的指令进行管理和输出。

##### 6.1.5 特殊的Value类：Function和BasicBlock类

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

##### 6.1.6 根节点：Module类

如同编译单元一样的存在，LLVM IR 文件的基本单位称为 `module`，我们的实验只有单`module`。

#### 6.2 架构设计

`llvm`生成及优化的流程如下：

```java
// IrBuilder.java
	@Override
    public void process() {
        // ============ 生成中间代码 ============
        compUnitNode.buildIr();             // 生成中间代码
        // ===== 中间代码分析 与 目标代码预处理 =====
        DeadCodeRemove.analyze();           // 简单的死代码删除
        ControlFlowGraphAnalyzer.analyze(); // 控制流图构建
        DomainTreeAnalyzer.analyze();       // domain树生成
        LoopAnalyzer.analyze();             // 循环分析
        if (Config.openMem2RegOpt) {
            new Mem2Reg().analyze();            // Mem2Reg重构带有phi的SSA
        }
    }
```

与中间代码生成有关的项目文件如下：

```
├── node		// 用于递归下降的node包
│   └── <文法非终结符>.java			// node类，放置有语法分析、错误处理、ir的递归下降方法	
├── ir			// 中间代码生成与优化
|   ├── analyze				// 中间代码优化
|   ├── types				// value的类型
|   ├── values				// values
|   |	└── constants				// Constant value们
|   │   └── instructions			// Instruction value们
|   |	└── BasicBlock.java			
|   |	└── Function.java			
|   |	└── GlobalVariable.java	
|   |	└── Module.java	
|   |	└── Value.java	
|   |	└── User.java		
|   ├── Irc.java					// 在中间代码生成中保存上下文信息
│   ├── Irbuilder.java				// 中间代码的入口类及工厂类
```

##### 6.2.1 node类们：递归下降的具体实现者

node类们内都实现了抽象父类的`buildIr()`方法，**用以进行基于属性翻译文法的递归下降。中间代码生成的具体逻辑都存于各种node中**。

```java
@Override
public void buildIr() {
    ...
}
```

##### 6.2.2 IrBuilder类：指令的工厂

IrBuilder类是中间代码的入口，同时也**封装有构建`llvm`元素的工厂模式方法**。

举例来说，我们现在正要取出一个指针类型变量所指空间中的内容，可以构建一条`Load`指令（当然如果是数组就需要GEP指令了），并将其插入当前所处的基本块。可以如此做：

```java
// LValNode.java，其对应文法LVal → Ident {'[' Exp ']'}，是指针的发祥地
// 取出该指针指向空间的内容
      Value fParamValue = IrBuilder.buildLoadInstruction(lvalValue, Irc.curBlock);
      // 没有[]，则直接原封不动传回，因为这就是形参
      if(expNodes.isEmpty()){
          Irc.synValue = fParamValue;
      }
// IrBuilder.java
/**
     * 构建加载指令
     * @param pointer 要加载的地址，从这个地址处读取操作数
     * @return 完成加载的Value
     */
    public static Load buildLoadInstruction(Value pointer, BasicBlock parent) {
        Load load = new Load(getNameString(), parent, pointer);
        parent.addInstruction(load);
        return load;
    }
```

##### 6.2.3 Irc类：递归下降的上下文信息

刚刚我们提到，要进行基于属性翻译文法的递归下降。既然是属性翻译文法，势必涉及到**综合属性（`synthesized attribute`)的向上传递**，以及**继承属性（`inherited attribute`）的向下传递**。

得益于递归下降的方法，我们可以直接在`node`类内，通过父子`node`间直接调用对方的相关`setter`或者`getter`来粗暴地实现信息传递。

但如果我们传递的链条很长呢？譬如我们在一个`LVal`内的信息，要经过好长好长的路，才能抵达真正需要该信息的`ConstExp`，这时候如果还要写那么多`setter`，未免也过于笨。

考虑到只有综合属性会出现这种长线的传递，我们可以在`Irc`里直接记录全局的，正在传递的综合属性（们）：

```java
//=========================== 综合属性 =================================
    /**
     * Value类型列表的综合属性 up向上传递
     */
    public static ArrayList<Value> synValueArray = null;
    /**
     * Value类型的综合属性 up向上传递
     */
    public static Value synValue = null;
    /**
     * int类型的综合属性 up向上传递
     */
    public static int synInt = -1190;
```

**中间代码的构建过程，是字面意义上拆东墙建西墙的过程，即在根据文法递归下降 扫描语法分析结果的同时，在另一边逐步（用动作符号）搭建起另一个`llvm`的体系。**

既然是同步在扫描两个体系，就应当有两组指针来记录扫描进度。**语法分析的扫描进度（由递归下降保证外，还有当前是否在扫描常量表达式，是否在扫描函数实参，是否正在经历循环），`llvm`的构建进度（即当前在搭建哪个函数，哪个基本块，）都应当得到记录**：

```java
 /**
     * 当前所在基本块
     */
    public static BasicBlock curBlock = null;
    /**
     * 当前所在函数
     */
    public static Function curFunction = null;
    /**
     * 当前是否正在计算 无变量常数表达式
     * 如果是，那么综合属性只需要传递syvInt，且计算情况有所减少
     */
    public static boolean isBuildingConstExp = false;
    /**
     * 当前是否在进行全局变量的初始化
     */
    public static boolean isBuildingGlobalInit = false;
    /**
     *  当前是否正在构建一个int类型的实参
     *  如果是，但是当前解析到的却是int*类型，那么需要load
     */
    public static boolean isBuildingPointerRParam = false;

    /**
     * 处理多重循环中的continue，
     * 栈顶的loopEndBlock即是当前层Continue跳转的对象
     */
    public static Stack<BasicBlock> loopEndBlockStack = new Stack<>();
    /**
     * 处理多次循环中的break
     * 栈顶的endBlock即是当前层break跳转的对象
     */
    public static Stack<BasicBlock> endBlockStack = new Stack<>();
```

##### 6.2.4 `llvm`元素

`llvm`元素主要放置在`ir/values`内。

该类主要存储了`llvm`体系下，该元素的要素及其相应的`getter`和`setter`。

此外，所有`llvm`元素都实现了`buildMips()`方法（及辅助方法），用于后续构建`mips`体系。

因此，**`llvm`元素类是贯穿了`llvm`初步生成，中端的`mem2reg`重构，`mips`初步生成**的重要类。

举例来说，我们在基本块`BasicBlock`类内，会记录这样的一些信息：

```java
/**
     * 指令序列
     */
    private final LinkedList<Instruction> instructions = new LinkedList<>();
/**
     * 前驱与后继块
     */
    private final HashSet<BasicBlock> preBlocks = new HashSet<>();
    private final HashSet<BasicBlock> sucBlocks = new HashSet<>();
/**
     * 支配者块
     */
    private final ArrayList<BasicBlock> domers = new ArrayList<>();
	...
```

---

#### 6.3 SSA初步处理

##### 6.3.1 SSA的概念

**静态单赋值**（Static Single Assignment, **SSA**）是编译器中间表示中的一种变量的命名约定。当程序中的每个变量都有且只有一个赋值语句时，称一个程序是 SSA 形式的。

**在`llvm`中，每个变量都在使用前都必须先定义，且每个变量只能被赋值一次（每个变量只能被初始化，不能被赋值）**。

举例来说，如果想要返回 `1 * 2 + 3` 的值，我们下意识地就会像这样写。

```
LLVM
%0 = mul i32 1, 2
%0 = add i32 %0, 3
ret i32 %0
```

但这样写实际上是错的，因为变量 `%0` 被赋值了两次。我们需要修改为：

```
LLVM
%0 = mul i32 1, 2
%1 = add i32 %0, 3
ret i32 %1
```

##### 6.3.2 phi形式的SSA与逃课形式的SSA

当涉及到分支语句时，SSA会遇到一些问题，以下面这个循环为例：

```
int main(){
    int i = 0;
    i = getint();
    int a = 1;
    a = getint();
    for(; i < 10; i = i + 1){
    	i = i + 2;
        a = a + 3;
    }
    return 0;
}
```

我们会发现，对于循环的归纳变量，其注定会有两处赋值。因此同样对于`i`和`a`，我们需要再来一个`i1`和`a1`来放置这第二个赋值。

那么问题来了，在`i1`和`a1`的汇聚点，到底该采取哪个赋值？这时候就必须用到`phi`指令。

`phi` 指令这个指令能够根据进入当前基本块之前执行的是哪一个基本块的代码来选择一个变量的值。

```
<result> = phi <ty> [<val0>, <label0>], [<val1>, <label1>] ...
```

有了`phi`，我们就可以写出代码：

```
define dso_local i32 @main() {
b0:
	%i3 = call i32 @getint()
	%i7 = call i32 @getint()
	br label %b9
b9:
	%p25 = phi i32 [ %i3, %b0 ],  [ %i16, %b11 ]
	%p24 = phi i32 [ %i7, %b0 ],  [ %i19, %b11 ]
	%i14 = icmp slt i32 %p25, 10
	br i1 %i14, label %b10, label %b12
b10:
	%i19 = add i32 %p24, 2
	%i22 = add i32 %p25, 3
	br label %b11
b11:
	%i16 = add i32 %i22, 1
	br label %b9
b12:
	ret i32 0
}
```

但实际上，`phi`指令的构建有亿点点复杂，这也太难写了！如果有一个能够摆脱SSA限制的办法就好了。

虽然不能直接赋值，但是假如**我的局部变量都存储在指针指向的地址内，我每一次给变量赋值，其实都是在写地址**呢？~~这就是`llvm`ts`SSA`zy~~！这样就摆脱了SSA的单一赋值限制！

```
define dso_local i32 @main() {
b0:
	%i5 = alloca i32
	%i1 = alloca i32
	store i32 0, i32* %i1
	%i3 = call i32 @getint()
	store i32 %i3, i32* %i1
	store i32 1, i32* %i5
	%i7 = call i32 @getint()
	store i32 %i7, i32* %i5
	br label %b9
b9:
	%i13 = load i32, i32* %i1
	%i14 = icmp slt i32 %i13, 10
	br i1 %i14, label %b10, label %b12
b10:
	%i18 = load i32, i32* %i5
	%i19 = add i32 %i18, 2
	store i32 %i19, i32* %i5
	%i21 = load i32, i32* %i1
	%i22 = add i32 %i21, 3
	store i32 %i22, i32* %i1
	br label %b11
b11:
	%i15 = load i32, i32* %i1
	%i16 = add i32 %i15, 1
	store i32 %i16, i32* %i1
	br label %b9
b12:
	ret i32 0
}
```

##### 6.3.2 局部变量的逃课SSA实现

这种生成存取内存的`llvm`有四个特点：

- 每个局部变量都变为了栈上分配的空间（变量在符号表中存的其实是其 `alloca` 指令）

- 每次对局部变量的读都变成了从内存空间中的一次读（在`LValNode`中实现，这也是为什么这里是指针的故乡之一！）

- 每次对局部变量的写都变成了对内存的一次写（每次更新一个变量的值都变成了通过 `store` 对变量所在内存的写）

- 获取局部变量的地址等价于获取内存的地址

需要进行的主要操作有：

- 局部变量定义时：

  - **`alloca`（分配变量的栈空间，返回指向该空间的指针）**
  - 将`alloca`记录进入符号表。

  - **`store`（给变量赋值，即向指针所指空间内进行写）**

  ```java
  // VarDefNode.java
  // 1.2 局部非数组变量
              else{
                  // 分配空间
                  Alloca alloca = IrBuilder.buildAllocaInstruction(new IntType(32), Irc.curBlock);
                  // <name, pointer> 加入符号表
                  IrSymbolTableStack.addSymbolToPeek(identToken.str, alloca);
                  // 有初值 那么进行store
                  if(initValNode != null){
                      initValNode.buildIr();
                      IrBuilder.buildStoreInstruction(Irc.synValue ,alloca, Irc.curBlock);
                  }
                  // 无初值也不必分配 其值未知
              }
  ```

- 局部变量使用时：

  虽然代码有点长（主要是数组处理、常量表达式处理、形参与实参处理搅在一起了），但核心思路比较简单：

  - LVal: 查符号表获得指定局部变量或形参的`alloca`指针
  - PrimaryExp: 对于LVal传上来的`alloca`指针，进行`load`处理

  ```java
  // primaryExpNode.java
  			// LVal
              // 指针只能从左值中得出
              if(lValNode != null) {
                  // 正在加载函数参数，且要求指针类型的value，则不进行load
                  // 需要消除标记，因为后续还可能再进入primaryExp
                  if(Irc.isBuildingPointerRParam){
                      Irc.isBuildingPointerRParam = false;
                      lValNode.buildIr();
                  }
                  // 要求int类型的value，此处应该检查load
                  // 如果是指针类型 那么进行加载
                  // 指针类型在通常状态下的加载，即在此实现
                  else{
                      lValNode.buildIr();
                      if(Irc.synValue.getType() instanceof PointerType){
                          Irc.synValue = IrBuilder.buildLoadInstruction(Irc.synValue, Irc.curBlock);
                      }
                  }
              }
  // LValNode.java
  /**
       * LVal一般返回指针类型的value，该指针是所求变量的地址。
       * 让上层PrimaryExp来判断是否进行加载
       * 如果isBuildingConstExp，那么一定返回synInt
       * 对于函数实参，其降维操作在此执行
       * @synInt      返回左值的ConstInt值. 前提：isBuildingConstExp.
       * @synValue    返回存储左值内容的指针（地址）
       */
      @Override
      public void buildIr() {
          // 查符号表获得左值对应的value
          Value lvalValue = IrSymbolTableStack.getSymbol(identToken.str);
          assert lvalValue != null;
  		... 
          // 左值为Pointer类型 需要进一步取值
          else{
              // 根据指针指向的类型，进行讨论
              ValueType valueType = IrTool.getPointingTypeOfPointer(lvalValue);
  
              // 指向int类型
              if(valueType instanceof IntType){
                  // 需要直接提取计算出常数 且为全局常量指针 则将计算出的常数使用synInt传递
                  if(Irc.isBuildingConstExp && lvalValue instanceof GlobalVariable){
                      ConstInt initValue = (ConstInt) (((GlobalVariable) lvalValue).getInitValue());
                      Irc.synInt = initValue.getValue();
                  }
                  // 否则直接向上传递指针，这一般是变量存储的地址
                  else{
                      Irc.synValue = lvalValue;
                  }
              }
              // 指向指针
              // 该pointer一定是当前左值所处函数的 [数组形参]
              // 例如f(int a[], int b[][2])中的 a和b
              // 因此一定不在buildingConstExp
              // lvalValue可能的类型有：
              // 一维数组i32* *
              // 二维数组[2 * i32]* *
              // 因为只有形参在满足SSA的时候 会通过alloca和store 来在本来的指针上多附加一层指针 以存储形参指针的值
              // 返回指针
              else if(valueType instanceof PointerType){
                  // 取出该指针指向空间的内容
                  // 即复原出形参（包括内容和类型）
                  // i32*，[2 * i32]*
                  Value fParamValue = IrBuilder.buildLoadInstruction(lvalValue, Irc.curBlock);
                  // 没有[]，则直接原封不动传回，因为这就是形参
                  if(expNodes.isEmpty()){
                      Irc.synValue = fParamValue;
                  }
                  // 一级[]，根据形参本身是一维还是二维，分为两种情况
                  else if(expNodes.size() == 1){
                      expNodes.get(0).buildIr();
                      Value indexValue = Irc.synValue;
                      // 根据index 向前挪动指针的值
                      Value ptrval = IrBuilder.buildGetElementPtrInstruction(fParamValue, indexValue, Irc.curBlock);
  
                      // 如果形参指向的是数组，那么说明形参是二维的。例如int a[][2] => [2 * i32]*，则形参指向[2* i32]
                      // 但这里只取了一维，也就是说希望传入a[1]
                      // 一定是作为函数的实参！
                      // 那么函数实参传入的类型应当是i32*
                      // 所以应当向下降维
                      if(IrTool.getPointingTypeOfPointer(fParamValue) instanceof ArrayType){
                          ptrval = IrBuilder.buildRankDownInstruction(ptrval, Irc.curBlock);
                      }
                      Irc.synValue = ptrval;
                  }
                  // 二级[]，只可能是对于二维数组形参int a[][2]的取值，结果应该为int类型
                  else{
                      expNodes.get(0).buildIr();
                      Value indexValue1 = Irc.synValue;
                      expNodes.get(1).buildIr();
                      Value indexValue2 = Irc.synValue;
                      Irc.synValue = IrBuilder.buildGetElementPtrInstruction(fParamValue, indexValue1, indexValue2, Irc.curBlock);
                  }
              }
              // 指向数组
              // 则应该为正常的局部数组或者全局数组
              else if(valueType instanceof ArrayType){
                  // 常量表达式 最后结果必然是ConstInt
                  // 两种情况：全局常量数组 局部常量数组
                  // 常量数组都已经存储在了对应的对象内，直接调取方法读取即可
                  // 返回synInt
                  if(Irc.isBuildingConstExp){
                      Constant initVal;
                      // 全局常量数组 是GlobalVariable形式
                      if(lvalValue instanceof GlobalVariable){
                          initVal = ((GlobalVariable) lvalValue).getInitValue();
                      }
                      // 局部常量数组 是alloca的形式
                      else{
                          initVal = ((Alloca) lvalValue).getInitValue();
                      }
                      // 初值数组已经被存储在initVal对象中，根据[]依次获取即可
                      // 此处仍然在buildingConstExp，因此从expNode中获取的是synInt
                      for(ExpNode expNode : expNodes){
                          expNode.buildIr();
                          initVal = ((ConstArray) initVal).getElements().get(Irc.synInt);
                      }
                      Irc.synInt = ((ConstInt) initVal).getValue();
                  }
                  // 非常量表达式
                  // 这里不再有存储好的初值调用，因此应该使用gep指令
                  // 返回指针synValue
                  else {
                      // 根据[]不断使用gep向下取值
                      for(ExpNode expNode : expNodes){
                          expNode.buildIr();
                          lvalValue = IrBuilder.buildGetElementPtrInstruction(lvalValue, ConstInt.ZERO(), Irc.synValue, Irc.curBlock);
                      }
                      // 特别地，需要判断一下int a[2][3] 只调用了 a[0]的情况 : 定是函数实参
                      // 此时result是指向数组的指针
                      // 那么需要进行降维传参
                      if(IrTool.getPointingTypeOfPointer(lvalValue) instanceof ArrayType){
                          lvalValue = IrBuilder.buildRankDownInstruction(lvalValue, Irc.curBlock);
                      }
                      Irc.synValue = lvalValue;
                  }
              }
          }
      }
  ```

- 函数形参接收时：

  特别地，**函数的形参也类似于局部变量，我们也要将其处理为SSA形式，在栈上分配空间并赋值以代之，否则还是会遇到我们上一节分析出来的问题**！

  - FuncFParamNode：读取形参的类型、名称，创建形参的Value
  - FuncFParamsNode： 对于每一个形参，都进行`alloca`，`store`，其中`store`的内容就是刚创建的形参的Value。符号表中存入的也是`alloca`对应的value

  ```java
  // FuncFParamNode.java
  	/**
       * 解析参数，并以ValueType的形式传入function，以记录参数
       */
      @Override
      public void buildIr() {
          ...
          // 将解析完成的参数类型传给curFunction，在curFunction内部构建参数的value
          Irc.curFunction.addArgByValueType(type, Irc.inInt);
      }
  // FuncFParamsNode.java
  	@Override
      public void buildIr() {
          for(int i=0; i<funcFParamNodes.size(); i++){
              Irc.inInt = i;
              funcFParamNodes.get(i).buildIr();
          }
          // 之前已经将参数加入了function对象
          // 使用刚刚解析好的函数参数，来构建SSA形式的参数加载语句
          ArrayList<Value> args = Irc.curFunction.getArgValues();
          for(int i=0; i<funcFParamNodes.size(); i++){
              Value arg = args.get(i);
              Alloca alloca = IrBuilder.buildAllocaInstruction(arg.getType(), Irc.curBlock);
              IrBuilder.buildStoreInstruction(arg, alloca, Irc.curBlock);
              // 在符号表中记录形参，其对应value为alloca，之后调用的时候要load
         	IrSymbolTableStack.addSymbolToPeek(funcFParamNodes.get(i).getIdentToken().str, alloca);
          }
      }
  ```

#### 6.5 GEP指令的使用

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

**符号表中存储的是<局部变量名, 存储局部变量值的指针(即alloca类型的Value类)>**。

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
> 以`int a[2][3]`为例（上面代码中的例子是`int a[3][2]`），假设a基地址0，容易计算出二维数组a大小为$4*2*3 = 24$，一维数组a[0]大小为$3*4=12$。现举例如下：
>
> - getelementptr(a, 0)会返回什么？
>
> 会返回0 *24 = 0，即a的地址。
>
> 其类型是`[2 x [3 x i32]]*`（即与a的类型相同）。
>
> - getelementptr(a, 1)会返回什么？
>
> 会返回1 * 24 = 1，即飞出去一整个a之后的地址，完全不在a数组之内。
>
> 其类型是`[2 x [3 x i32]]*`（即与a的类型相同）。
>
> - getelementptr(a, 0, 0)会返回什么？
>
> 会返回0*24 + 0 * 12 = 0 ，即a[0]的地址。其类型是`[3 x i32]*`（即脱掉了最外层的[]，但是地址的值不变。因此该指令经常用来转变一个指针的类型）
>
> - getelementptr(a, 0, 1)会返回什么？
>
> 会返回0*24 + 1 * 12 = 12，即a[1]的地址。
>
> 其类型是`[3 x i32]*`。
>
> - getelementptr(a, 666, 233)会返回什么？
>
> 会返回666*24 + 233 * 12，即直接飞出了666个a，再飞出去233个a[0]那么大。
>
> 其类型是`[3 x i32]*`。

再回头看我们的代码。

下面这两条指令是在把`[2 x [3 x i32]]*`转变为`i32*`类型，因为我们要向数组元素内存入常数，而数组元素是int类型的。

```
%instr2 = getelementptr [3 x [2 x i32]], [3 x [2 x i32]]* %instr1, i32 0, i32 0
%instr3 = getelementptr [2 x i32], [2 x i32]* %instr2, i32 0, i32 0
```

之后就是不断地移动指针，然后在该地址内存入常数。例如这里是向第6个元素（即`a[2][3]`中存储元素）。可以看到，初始化元素时需要展平数组。

```llvm
%instr13 = getelementptr i32, i32* %instr3, i32 5
store i32 60, i32* %instr13
```

部分Java代码如下：

```java
// node/VarDefNode.java
// VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
            // 局部数组变量
            // 解析维数信息
            ArrayList<Integer> dims = new ArrayList<>();
            for(ConstExpNode constExpNode : constExpNodes){
                constExpNode.buildIr();
                dims.add(Irc.synInt);
            }
            ArrayType arrayType = new ArrayType(new IntType(32), dims);
                // 分配空间
                Alloca arrayPointer = IrBuilder.buildAllocaInstruction(arrayType, Irc.curBlock);
                // 加入符号表
                IrSymbolTableStack.addSymbolToPeek(identToken.str, arrayPointer);

                // 有初始值
                // 处理类似于局部常量数组
                // FlattenArray通过syvValueArray传递上来
                if(initValNode != null){
                    initValNode.setDims(dims);
                    // 解析初始化值，通过Irc.synValueArray传上来
                    initValNode.buildIr();
                    // 使用store和gep将展平后的内容数组存入数组
                    IrBuilder.buildStoreWithValuesIntoArray(arrayPointer, dims, Irc.synValueArray, Irc.curBlock);
                }
                // 无初始值，不予处理
/**
     * 构建GEP和store指令，将指定的flatten value array，存入局部数组
     * @param arrayPointer  目标数组指针
     * @param dims          数组维数信息
     * @param flattenArray  要存的value内容数组（展平）
     */
    public static void buildStoreWithValuesIntoArray(Alloca arrayPointer, ArrayList<Integer> dims, ArrayList<Value> flattenArray, BasicBlock parent){
        // 接下来获取一个指向底层元素的指针，挨个存入元素
        GetElementPtr basePtr = IrBuilder.buildRankDownInstruction(arrayPointer, parent);
        for(int i=1; i<dims.size(); i++){
            basePtr = IrBuilder.buildRankDownInstruction(basePtr, parent);
        }

        // 遍历展平之后的数组
        // 依次将数组内的元素使用store进行存储，存储位置为base + i
        GetElementPtr elementPtr = basePtr;
        IrBuilder.buildStoreInstruction(flattenArray.get(0), elementPtr, parent);
        for(int i=1; i < flattenArray.size(); i++){
            // p = base + i
            elementPtr = IrBuilder.buildGetElementPtrInstruction(basePtr, new ConstInt(32, i), parent);
            IrBuilder.buildStoreInstruction(flattenArray.get(i), elementPtr, parent);
        }
    }
```

#### 6.6 ConstExp的处理与常量传播

在我们的文法当中，有这样的声明：

> 常量表达式 ConstExp 
>
> ConstExp -> AddExp 中使用的 ident 必须是常量

何为常量？即能够在编译阶段就确定值的量，包括了常量标识符与立即数。**对于常量，我们可以在编译阶段就计算出常量表达式的值，以及将常量标识符替换为其值**。

为此，我们在`Irc`里设置有字段，在`Exp`系的`buildIr`内，构建常量时的分类讨论的方式有所不同，且由于能够计算出int类型具体值，我们只需要进行`synInt`综合属性的传递。

```java
/**
     * 当前是否正在计算 无变量常数表达式
     * 如果是，那么综合属性只需要传递syvInt，且计算情况有所减少
     */
    public static boolean isBuildingConstExp = false;
```

常量分为数组常量和非数组常量，其行为有所不同：

|                          | int常量       | 数组常量                             |
| ------------------------ | ------------- | ------------------------------------ |
| 是否在llvm代码中显式声明 | 否            | 与普通数组变量一致，采用alloca,store |
| 在`ConstExp`下           | 直接求出int值 | 直接求出指定元素的int值              |
| 非`ConstExp`下           | 直接求出int值 | 与普通数组变量一致，采用gep，load    |

给出如下例子：

```c
const int a[2] = {1,20};
int main(){
    const int b[a[1]] = {3,4};
    const int c = 0;
    printf("%d%d%d",a[c],b[1],c);
    return 0;
}
```

```llvm
@a = dso_local constant [2 x i32] [i32 1, i32 20]

declare i32 @getint()
declare void @putstr(i8*)
declare void @putint(i32)
declare void @putch(i32)
define dso_local i32 @main() {
b0:
	%i1 = alloca [20 x i32]
	%i2 = getelementptr [20 x i32], [20 x i32]* %i1, i32 0, i32 0
	store i32 3, i32* %i2
	%i4 = getelementptr i32, i32* %i2, i32 1
	store i32 4, i32* %i4
	%i6 = getelementptr [2 x i32], [2 x i32]* @a, i32 0, i32 0
	%i7 = load i32, i32* %i6
	%i8 = getelementptr [20 x i32], [20 x i32]* %i1, i32 0, i32 1
	%i9 = load i32, i32* %i8
	call void @putint(i32 %i7)
	call void @putint(i32 %i9)
	call void @putint(i32 0)
	ret i32 0
}
```

#### 6.7 符号表的构建

类似错误处理，又重新搭了一遍栈式符号表。

#### 6.8 其他

其他关键的设计还有跳转关系的构建（短路求值+for循环的实现)、函数形参的处理、变量类型（`ir/type`包）

还有3h就得交文档了，优化文档还没写，因此其余部分具体逻辑详见代码，代码中有详细注释。急急急。

## 7. 目标代码生成与优化

任务是以此前生成的中间代码（树形结构表示的`llvm`)作为输入，输出与源程序语义等价的`mips`代码。 

在llvm中，**我们已经将源代码转换成了很接近中间代码的形式了：我们划分并生成了基本块，生成了几乎能一等一转化为mips的llvm指令**。因此在目标代码的**初步**生成的过程中，我们的关注点主要在于**存储管理、指令的等价翻译**。

#### 7.1 架构设计

后端的流程如下：

```java
	@Override
    public void process() {
        // 生成带有虚拟寄存器的目标代码
        irModule.buildMips();
        // 寄存器分配
        if(Config.openRegAllocOpt){
            RegBuilder regBuilder = new RegBuilder();
            regBuilder.buildRegs();
            // 窥孔优化
            if(Config.openPeepHoleOpt){
                Peephole peephole = new Peephole();
                peephole.doPeephole();
            }
        }
    }
```

与目标代码生成有关的项目文件如下：

```
├── ir			// 中间代码生成与优化
|   ├── analyze				// 中间代码优化
|   ├── types				// value的类型
|   ├── values				// values
|   |	└── constants				// Constant value们
|   │   └── instructions			// Instruction value们
|   |	└── BasicBlock.java			
|   |	└── Function.java			
|   |	└── GlobalVariable.java	
|   |	└── Module.java	
|   |	└── Value.java	
|   |	└── User.java		
|   ├── Irc.java					// 在中间代码生成中保存上下文信息
│   ├── Irbuilder.java				// 中间代码的入口类及工厂类
├── backend		// Mips生成与优化 
|   ├── instructions		// Instruction们
|   ├── operands			// 操作数们，包括立即数，虚拟、物理寄存器，标签
|   ├── opt					// 目标代码优化
|   |	└── BlockliveVarInfo.java	// 活跃变量分析
|   │   └── RegBuilder.java			// 图着色寄存器分配
|   |	└── Peephole.java			// 窥孔优化
|   ├── Mc.java						// 在目标代码生成中保存上下文信息
|   └── MipsBuilder.java			// 目标代码的入口及指令的工厂类
```

##### 7.1.1 MipsBuilder：指令的工厂类

在MipsBuilder中封装有构建各种指令、构建操作数的工厂模式方法。

例如构建一条move指令并加入指定的`MipsBlock`：

```java
// MipsBuilder.java
    /**
     * 构建move指令
     */
    public static MipsMove buildMove(MipsOperand dst, MipsOperand src, BasicBlock irBlock){
        MipsMove move = new MipsMove(dst, src);
        Mc.b(irBlock).addInstruction(move);
        return move;
    }
// Add.java
		// op1 op2均为常数 则加法可以直接变为move dst Imm(op1+op2)
        if (op1 instanceof ConstInt && op2 instanceof ConstInt) {
            int imm1 = IrTool.getValueOfConstInt(op1);
            int imm2 = IrTool.getValueOfConstInt(op2);
            MipsBuilder.buildMove(dst, new MipsImm(imm1 + imm2), getParent());
        }
```

##### 7.1.2 Mc：记录`llvm`成分到`mips`成分的映射

我们是在llvm的成分类中进行的mips生成，因此需要将llvm成分与mips成分进行映射。

```java
/**
     * 获取ir函数对象 对应的 mips函数对象
     * @param irFunction    ir函数对象
     * @return              mips函数对象
     */
    public static MipsFunction f(Function irFunction){
        return functionMap.get(irFunction);
    }
    /**
     * 获取ir基本块对象 对应的 mips基本块对象
     * @param irBlock   ir基本块对象
     * @return          mips基本块对象
     */
    public static MipsBlock b(BasicBlock irBlock){
        return blockMap.get(irBlock);
    }

    /**
     * 获取ir Value对象 对应的 mipsOperand对象
     * @param irValue   ir Value对象
     * @return          mipsOperand对象
     */
    public static MipsOperand op(Value irValue){
        return opMap.get(irValue);
    }

    /**
     * 查询在mipsBlock内，op1/op2 是否已有计算结果
     * @return          计算结果的mipsOperand对象
     */
    public static MipsOperand div(MipsBlock mipsBlock, MipsOperand op1, MipsOperand op2){
        return divMap.get(new Triple<>(mipsBlock, op1, op2));
    }
```

##### 7.1.3 `llvm`的成分类：mips生成的主要场所

通过遍历`llvm`的树形结构来生成`mips`，遍历在`ir/values`下的各个value类进行，他们都实现了父类`Value`的`buildMips()`方法。

##### 7.1.4 `backend/instructions`: Mips指令类

指令类都继承了`MipsInstruction`类，**该类内有`src`操作数和`dst`操作数的相应管理方法，包括`use`，`def`的记录，用于后续寄存器分配时，对虚拟寄存器进行查询、替换**。

##### 7.1.5 `backend/operands`: 操作数类

该包内的类均可以作为`mips`指令的操作数，具体来说有立即数、虚拟寄存器、物理寄存器、标签。

物理寄存器的相关配置在`RegType`枚举类中，记录了物理寄存器的编号、名称、何者需要在函数调用时保存、何者能够作为全局寄存器分配等信息。

#### 7.2 构建流程

带有虚拟寄存器的mips的总体构建流程如下：

##### 7.2.1 构建.data段

构建.data段主要是在翻译`llvm`的全局变量元素`GlobalVariable`。

先前在`llvm`生成过程中，我们将需要`printf`输出的字符串重新分配为了全局常量字符串，因此这里全局变量共有三类：字符串、int变量、int数组。依次构建`mipsGlobalVariable`，然后加入`MipsModule`即可。

```java
// GlobalVariable.java
@Override
    public void buildMips(){
        MipsGlobalVariable mipsGlobalVariable = null;
        ...
        // 未初始化的int数组
        else if(initValue instanceof ZeroInitializer){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), initValue.getType().getSize());
        }
        // 常量字符串
        else if(initValue instanceof ConstString){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), ((ConstString) initValue).getContent());
        }
        // int变量
        else if(initValue instanceof ConstInt){
            mipsGlobalVariable = new MipsGlobalVariable(getName(), new ArrayList<>(){{
                add(((ConstInt) initValue).getValue());
            }});
        }
        // int数组
        else if(initValue instanceof ConstArray){
            ArrayList<Integer> ints = new ArrayList<>();
            for (Constant element : ((ConstArray) initValue).getFlattenElements()){
                ints.add(((ConstInt) element).getValue());
            }
            mipsGlobalVariable = new MipsGlobalVariable(getName(), ints);
        }
        MipsModule.addGlobalVariable(mipsGlobalVariable);
    }
```

##### 7.2.2 为所有Block和Function创建Mips对象，并映射到llvm的相应对象

没有太多可说的，作用主要是方便在后续遍历语句时，能够方便地引用函数和基本块（用于call、br等llvm指令的翻译）

```java
/**
     * 将中间代码的函数和基本块对象:
     * 1.构建mips里的相应对象
     * 2.加入Module
     * 3.信息存储到mips对象里
     */
    private void mapFunctionBlockIrToMips(){
        // 遍历所有函数
        for (Function irFunction : functions){
            // 构建函数对象
            MipsFunction mipsFunction = new MipsFunction(irFunction.getName(), irFunction.isLibFunc());
            Mc.addFunctionMapping(irFunction, mipsFunction);
            MipsModule.addFunction(mipsFunction);
            // 构建基本块对象
            ArrayList<BasicBlock> blocks = irFunction.getBasicBlocks();
            for (BasicBlock irBlock : blocks){
                MipsBlock mipsBlock = new MipsBlock(irBlock.getName(), irBlock.getLoopDepth());
                Mc.addBlockMapping(irBlock, mipsBlock);
            }
            // 记录mipsBlock的前驱块信息, 前驱块当然也是mipsBlock
            for (BasicBlock irBlock : blocks){
                MipsBlock mipsBlock = Mc.b(irBlock);
                for(BasicBlock irPreBlock : irBlock.getPreBlocks()){
                    mipsBlock.addPreBlock(Mc.b(irPreBlock));
                }
            }
        }
    }
```

##### 7.2.3 遍历`llvm`的树形结构

依序遍历`llvm`的所有函数、所有基本块、所有指令，进行翻译。

#### 7.3 存储管理的翻译

在`llvm`到`mips`的翻译过程中，与原指令或llvm成分长相完全不同的Mips成分，当属存储管理。具体点来说，是函数调用与返回的存储管理。

##### 7.3.1 Call：参数传递

Call的作用是调用函数，理所当然地，在mips中需要我们手动进行实参的传递，同时记录`MipsCall`指令对于寄存器的修改（即def）。

对于前四个参数，保存在`a0`-`a3`里即可。对于更多的参数，需要保存在栈上。

调用函数在调用者处的准备工作，都由Call进行翻译。

```java
@Override
    public void buildMips() {
        MipsBlock mipsBlock = Mc.b(getParent());
        MipsFunction mipsFunction = Mc.f(function);
        // 先构建出call指令，后续要记录该指令用到的A寄存器
        // ！这也是唯一一次使用野生未封装的new MipsInstruction
        MipsInstruction call;
        // 内建函数，需要宏调用
        if (function.isLibFunc()) {
            call = new MipsMacro(mipsFunction.getName());
            // 系统调用必然改变 v0, v0加入def
            call.addDefReg(MipsRealReg.V0); // TODO: addDefReg 双参数修改为单参数
        }
        // 非内建函数，直接构建jal指令即可
        else {
            call = new MipsCall(mipsFunction);
        }

        // 进行传参, 遍历所有irValue参数
        int argc = getArgs().size();
        for (int i = 0; i < argc; i++) {
            Value irArg = getArgs().get(i);
            MipsOperand src;
            // 前四个参数存储在a0-3内
            if (i < 4) {
                src = MipsBuilder.buildOperand(irArg, true, Mc.curIrFunction, getParent());
                MipsMove move = MipsBuilder.buildMove(new MipsRealReg("a" + i), src, getParent());
                // 加入use，保护寄存器分配时不消除move
                call.addUseReg(move.getDst());
            }
            // 后面的参数先存进寄存器里，再store进内存
            else {
                // 要求存入寄存器
                src = MipsBuilder.buildOperand(irArg, false, Mc.curIrFunction, getParent());
                // 存入 SP - 4 * nowNum 处
                MipsImm offsetOperand = new MipsImm(-(argc - i) * 4);
                MipsBuilder.buildStore(src, MipsRealReg.SP, offsetOperand, getParent());
            }
        }

        // 栈的生长
        if (argc > 4) {
            // 向下生长4 * allNum: SP = SP - 4 * allNum
            MipsOperand offsetOperand = MipsBuilder.buildImmOperand(4 * (argc - 4), true, Mc.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.SUBU, MipsRealReg.SP, MipsRealReg.SP, offsetOperand, getParent());
        }

        // 参数准备妥当后，再执行jal指令
        mipsBlock.addInstruction(call);

        // 这条语句执行完成的场合，恰是从函数中返回
        // 栈的恢复 与生长相反，做加法即可
        if (argc > 4) {
            MipsOperand offsetOperand = MipsBuilder.buildImmOperand(4 * (argc - 4), true, Mc.curIrFunction, getParent());
            MipsBuilder.buildBinary(MipsBinary.BinaryType.ADDU, MipsRealReg.SP, MipsRealReg.SP, offsetOperand, getParent());
        }
		...
    }
```

##### 7.3.2 MipsFunction：保存现场

MipsFunction是Mips函数对象。

在与Call的关系中，MipsFunction是被调用的一方，由MipsFunction负责保存现场。

如下方法能够记录在本函数内有改动（def）的寄存器，同时计算栈帧大小。这些寄存器在返回调用者后可能有改变，需要保存在栈帧内。

```java
/**
     * 栈上的空间从上到下依次为：
     * 1.调用者保存的寄存器
     * 2.其他alloca
     * 3.参数alloca
     */
    public void rebuildStack() {
        // 遍历下属所有语句，记录所有用过的寄存器，作为函数调用前要保存的现场
        for (MipsBlock block : blocks) {
            for (MipsInstruction instruction : block.getInstructions()) {
                // 保存写过的寄存器(的类型)
                for (MipsOperand defReg : instruction.getDefRegs()) {
                    if (defReg instanceof MipsRealReg) {
                        RegType regType = ((MipsRealReg) defReg).getType();
                        if (RegType.regsNeedSaving.contains(regType)) {
                            regsNeedSaving.add(regType);
                        }
                    } else {
                        System.out.println("[MipsFunction] defReg中混入了非物理寄存器！");
                    }
                }
            }
        }
        // 需要分配的用于保存现场的空间
        int stackRegSize = 4 * regsNeedSaving.size();
        // 总的空间大小：alloca空间 + 保存现场的空间
        totalStackSize = stackRegSize + allocaSize;
        // 更新先前记录的 保存在栈上的参数 的位移
        for (MipsImm argOffset : argOffsets) {
            int newOffset = argOffset.getValue() + totalStackSize;
            argOffset.setValue(newOffset);
        }
    }
```

保存现场的具体代码则直接放在了`MipsFunction`的打印处，实际上没有加入指令序列。

```java
/**
     * 需要打印：
     * 函数 label
     * 保存被调用者寄存器
     * 移动栈指针 sp
     * 基本块的mips代码
     */
    @Override
    public String toString() {
        if (isLibFunc) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        // 非主函数需要保存寄存器
        if (!name.equals("main")) {
            // 保存现场
            int stackOffset = -4;
            for (RegType regType : regsNeedSaving) {
                // 保存位置：-stackOffset($SP)
                sb.append("\t").append("sw\t").append(regType).append(",\t")
                        .append(stackOffset).append("($sp)\n");
                // 继续向下生长
                stackOffset -= 4;
            }
        }
        // $SP = $SP - totalStackSize
        if (totalStackSize != 0) {
            sb.append("\tadd\t$sp,\t$sp,\t").append(-totalStackSize).append("\n");
        }
        // 生成基本块的mips
        for (MipsBlock block : blocks) {
            sb.append(block);
        }

        return sb.toString();
    }
```

##### 7.3.3 MipsRet：恢复现场

MipsRet是Mips的返回指令对象，由llvm的Ret指令直接翻译而来。

Ret对象会记录其所属的MipsFunction，以方便地取用寄存器的保存信息。

同样地，恢复现场的具体代码则直接放在了`MipsRet`的打印处，实际上没有加入指令序列。

```java
@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int stackSize = function.getTotalStackSize();
        // 返回前将SP复位
        if (stackSize != 0) {
            sb.append("add\t$sp, \t$sp,\t").append(stackSize).append("\n");
        }
        // 主函数直接结束运行
        if (function.getName().equals("main")) {
            sb.append("\tli\t$v0,\t10\n");
            sb.append("\tsyscall\n\n");
        }
        // 非主函数，需要恢复现场
        else {
            // 在返回之前回复寄存器寄存器
            int stackOffset = -4;
            for (RegType regType : function.getRegsNeedSaving()) {
                sb.append("\t").append("lw\t").append(regType).append(",\t").append(stackOffset).append("($sp)\n");
                stackOffset -= 4;
            }
            // 跳转返回
            sb.append("\tjr\t$ra\n");
        }
        return sb.toString();
    }
```

