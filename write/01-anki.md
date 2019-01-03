
[QS Primer: Spaced Repetition and Learning - Quantified Self](http://quantifiedself.com/2012/06/spaced-repetition-and-learning/)

> Memorization is only a small part of learning, but it in many circumstances it is unavoidable. There is an ideal moment to practice what you want to memorize. Practice too soon and you waste your time. Practice too late and you’ve forgotten the material and have to relearn it. The right time to practice is just at the moment you’re about to forget. If you are using a computer to practice, a spaced repetition program can predict when you are likely to forget an item, and schedule it on the right day.


---

[SuperMemo as a new tool for programmers](https://www.supermemo.com/articles/programming.htm)

这是一篇论文，关于使用 SuperMemo 来学习计算机知识的。

+ Foundations of computing sciences: algorithms, data structures, computational complexity, probability calculus, operation research, numerical methods, software engineering, statistics, etc.
+ Elements of the operating system and the assembly language (in the considered case: DOS, Windows and the 80*86).
+ Elementary terminology and concepts of the development environment (for example, in Windows: client area, control menu, system key, modal vs. modeless dialog, child vs. popup window, logical vs. device coordinates, interface object vs. interface element, virtual method table, polymorphism, etc.)
+ Programming tools (debugger, profiler, resource workshop, help compiler, etc.)
+ Elements of the language; syntax and use: procedures, functions, variables, constants, objects and their hierarchy, fields, methods, messages, run-time errors (but not compiler errors), etc.
+ Practical tips, observations, and experimental findings not included in the documentation (these should be collected by the programmer in the course of the programming practice). For example, compiler bugs, freaks of the operating system, frequently encountered traps (e.g. using more than five device contexts in the GDI), etc.

---

[Memorizing a programming language using spaced repetition software | Derek Sivers](https://sivers.org/srs)

有一条非常有意思的评论：

> I do the same thing. I started just using anki cards, but found that using the normal anki cards to be less effective because thinking the answer to the card and programming are different contexts, making it harder to apply the anki knowledge, so I make a modification and now use anki to remind me to do actual programming tasks. I have a git repo with various exercises. https://github.com/dnuffer/dpcode
> In the anki deck I record how long it took me to do each exercise and then when I re-do it I compare the times and use my improvement or lack thereof to determine the next interval.

> I've spent decades as a language learning consultant (real human language), so reading this got me to thinking. I don't know any programming languages, although I used to write pretty complex macros in WordPerfect years and years ago. *smile*
> It seems to me that what you're talking about here is a hybrid kind of learning. There is vocabulary and syntax, as in any language, but the programmer is also learning new definitions for English words (e.g. I have no idea what the English examples which speak of "arrays" are talking about!). Plus, there are other conceptual layers to be learned. In other words, first the programmer needs to know what they can/need do, then how to "say" a command in one of a variety of langue ages to make it happen. The conceptual part is primary, and doesn't seem like it would to all be relearned to learn a second or third programming language, which many people do.
> Now, on to language learning. Spaced Repetition is indeed a valid principle for learning and memorizing, and even for learning language, but the mistake is to think at language is learned simply by memorization. It isn't.
> There would be more effective ways to "review" programming commands, and to use Spaced Repetition than to use flash cards (paper or electronic). These other, situationally-based, interactive and cognitive activities would produce even better (and more long-lasting) results/learning, and would take advantage of other, equally valid principles of learning, such as the Iceberg Principle.
> Anyway, you've got my mind running on how it would be to apply those language learning principles to a very different kind of language (programming)!


# 背景
我是一个中级程序员。我没有在学校学习编程。我只是根据需要自学编程，原因是我有一个网站，用户量一直在增长，但我没钱雇一个程序员，所以我自己看了一些关于 PHP，SQL，Apache 的书，学到一些刚好足够让网站工作的知识，然后就将这些知识用了很多年。

但现在我跟一个真正的程序员一起工作，他的词汇总是让我很惊讶。那些命令和函数总是毫不费力地从他的指下碰出来。我们使用同样的语言，但他记住了这么多，对他而言我就像大学教授旁边的一个小孩。我也像他那样熟练。

我想起过去很多年自己学习了很多东西，然后马上忘记。我读了关于某些有用特性的一些书或文章，尝试一下，然后被其他东西吸引，就搞忘了。接下来又用老样子来做事情。

我想牢牢地记住命令和语言技巧，而不会忘记，这样当我需要它们时它们就会立刻出现在我的脑海。

# Spaced Repetition
你应该听过这样的一个事实，除非你不断记起，否则会很快遗忘。

你自己沉浸在某个语言当中时可能会偶然碰到这种情况，比如，你学习过的新单词会偶然记起。

但记忆研究表明最有效最省时的记忆方式是在刚好要遗忘前再次记起某事。

![](https://sivers.org/images/forgetting-curve-srs.jpg)

比如，你学习了一个新单词，你在几分钟后听到它，于是你复习一次。然后是几小时，然后是过一天，再过两天，过5天，过10天，3周，6周，3个月，8个月等等。此后它就成了一个永久记忆。

Spaced Repetition 软件正是以这样一个周期让你进行记忆。你可以向软件输出一些你想要记住的东西，然后你每过一段时间测试一遍，测试间隔由软件根据你的反馈来决定。每测试一个问题，如果你回答说这个测试很简单，那这次测试很久之后才会再次进行。如果你回答说这个测试很难，那这次测试会在几分钟后再次出现，直到你记住为止。

如果你能记住你选择的编程语言中的每一件事情，那会怎样？每一个命令，每一个参数，每一个方法。几百种常见问题的每个解决方案，每个都被记住了，随时可供你使用？想想浏览文档或书籍时，能永久记住其中每个细节么？

好吧，介绍完了。现在看看该怎么做吧。

# 学习
**用卡片记住你在学习的东西**

在你创建卡片之前，你应该学习并理解它。只有当你真的理解之后才创建卡片。

(这也是为什么使用别人的卡片时用处不大)

# Convert Knowledge into Small Facts:

你会制作很多卡片。正面是问题，答案在背面。

如果你只是记外语单词，那么卡片的格式非常简单。正面是单词或短语，背面是它的翻译。或者反过来也行。

![](https://sivers.org/images/anki-cn-1.png)

![](https://sivers.org/images/anki-cn-2.png)

![](https://sivers.org/images/anki-cn-3.png)

![](https://sivers.org/images/anki-cn-4.png)

但你在学习其他东西时，你花点工夫来制作你自己的卡片。

花时间阅读你想记住的材料，找出关键点，将它们处理成最小形式，然后转换成以后用于测试自己的问题。

以下是我坚持一年使用这种记忆方法后得来的经验，这个经验可以为你节省很多时间：

# Turn prose into code
假设你正在阅读编程教程，你遇到一段正在描述某个特性：

“The add (+) operator... if only one operand is a string, the other operand is converted to a string and the result is the concatenation of the two strings.”

你自己验证过这个特性后，理解它了。那么就使用一张卡片记住这个特性。

```ruby
  var a = 5 + '5';
  // what is a?
```

```
  '55'
  If either side of + is a string, the other is
  converted to a string before adding like strings.
```

# Try to trick your future self
有时你会学到一些 "gotcha"，一个常见错误或不符合常理的特性。

“If the new Array constructor is passed a single number, it creates an empty Array with a length of that number. Any other combination of arguments creates an Array of those arguments.”

你自己验证过这个特性后，理解它了。那就制作两张卡片来检查是否记住了：

```ruby
  var a = new Array('5');
  // what is a?
```

```
  An array with one item, the string '5': ['5'];
```

几乎是一模一样的问题：

```ruby
  var a = new Array(5);
  // what is a?
```

```
  An empty array with a length of 5.
```

程序使用卡片对你进行测试时它会打乱顺序。

也可以使用一些更复杂的例子来考查自己，以使自己记住 gotcha 

```ruby
  var a = [20, 10, 5, 1];
  // what is a.sort()?
```

```
  [1, 10, 20, 5]
  // sort treats all values as strings
```

记得还要 **quiz yourself on the solution**

```ruby
  var a = [20, 10, 5, 1];
  // sort these in numeric order
```

```ruby
  function compare(v1, v2) { return(v1 — v2); }
  a.sort(compare);
```

# Save the cool tricks
如果你找到一个很酷的 trick，你想记住它。可以将它作为一个小小挑战的答案。

```ruby
  var albums = [
    {name: 'Beatles', title: 'White Album', price: 15},
    {name: 'Zeppelin', title: 'II', price: 7}];
  // make this work:
  albums.sort(by('name'));
  albums.sort(by('title'));
  albums.sort(by('price'));
```

```ruby
  function by(propName) {
    return function(obj1, obj2) {
      v1 = obj1[propName];
      v2 = obj2[propName];
      if (v1 < v2) { return -1; }
      else if (v1 > v2) { return 1; }
      else { return 0; }
    };
  }
```

# Make the answer require multiple solutions
如果一个问题有多种答案，而你想记住每一种答案，以便以后可以使用多种方案。

```ruby
  s = 'string like this'
  # In Ruby, show two ways to turn it into 'String Like This'
```


```
  s.split.map(&:capitalize).join(' ')
  s.gsub(/\b\S/) {|x| x.upcase}
```

有时你花20分钟记住概念性的东西还不如记住一些特定的函数。有时你只是需要一些简明的例子让自己记起那些概念。

```ruby
  /(a(b)((c)d))/.match('abcd')
  # What will $1, $2, $3, $4 be?
```

```
  $1 = 'abcd'
  $2 = 'b'
  $3 = 'cd'
  $4 = 'c'
```

另一个例子

```ruby
  class C
    self
  end
  class D < C
  end
  d1 = D.new
  # which object is self?
```

```
class D
```

读读这篇文章 [Effective learning: Twenty rules of formulating knowledge | SuperMemo.com](https://www.supermemo.com/en/articles/20rules)

# Run Through it Daily
为了得到最有效的结果，你应该每天都打开软件。如果太长时间不看，你会弄混时间然后不得不重新学习那些你已经记住的东西。

你可以每天只花20分钟就记住上千个类似的 facts

我就当它是早上的一个例行活动，煮一杯茶，边记Anki边喝茶。

---

[Effective learning: Twenty rules of formulating knowledge | SuperMemo.com](https://www.supermemo.com/en/articles/20rules)

学习的速度依赖于你如何整理学习资料。如果学习资料整理得好，你的学习速度会快。以下规则是按重要性列出来的。前面列出的规则被违反得最多，但遵守这些规则获得的收益也最大。

学习过程中的20条整理资料的规则。

# Do not learn if you do not understand
不理解时不要学。

试着学你不理解的东西最终是无意义的。很多学生犯下这种不理解就学的罪行。因为他们没办法。原因是书的质量不高，或者考试马上就要到了。

如果你不说德语，虽然也可以学习德语写的历史书，但对你来说书中不过是塞进一个又一个单词。学习这本书的时间是天文数字。更重要的是，学习到的知识却几乎可以忽略。

德语历史书的例子可能过于极端。但很多你在学习的资料看似组织良好，但你却缺乏理解。很快你的学习进程就被一大堆无用的学习资料污染，这堆资料让你误以为它们将来某天会有用。

# Learn before you memorize
记之前先学习。

在你开始记忆一些单个的事实和规则之间，先对你要学习的知识建立全貌 ( **build an overall picture of the learned knowledge** )。只有当单个的知识块可用于构建整体上连贯一致的结构时，你才可以极大地减少学习时间。这跟规则一中提到的问题理解非常相关。单个的知识块就像德语历史书中的一个单词。

不要记忆联系上非常松散的事实。可以像下面这样做：

+ First read a chapter in your book that puts them together (e.g. the principles of the internal combustion engine). 
+ Only then proceed with learning using individual questions and answers (e.g. What moves the pistons in the internal combustion engine?), etc.

# Build upon the basics
基于基础进行构建。

整体的知识图不应该过于完整至所有细节。相反，知识图越简单越好。书的初始章节越短越好。简单的模型容易理解和完成。以后也能基于这些进行进行一步的构建。

不要忽视基础。记住貌似理所当然的事情并不是浪费时间。基础更容易被忘记，而记住简单事情的代价很低。宁缺毋滥。Remember that usually you spend 50% of your time repeating just 3-5% of the learned material 记住，你花了50%的时间反复记忆所学资料 3-5% 的内容。  [source](https://www.supermemo.com/articles/theory.htm)。而基础的东西往往很容易记住，只占总时间很少一部分，但忘记基础东西的代价很大。

# Stick to the minimum information principle
坚持最小信息原则。

你学习的资料必须尽可能简单化。

## Simple is easy
简单的东西容易。简单的东西容易记住。事实上大脑可以更容易以相同的方式处理简单的东西。

## Repetitions of simple items are easier to schedule
简单的东西容易反复记忆。

Very often, inexperienced students create items that could easily be split into ten or more simpler sub-items! Although the number of items increases, the number of repetitions of each item will usually be small enough to greatly outweigh the cost of (1) forgetting the complex item again and again, (2) repeating it in excessively short intervals or (3) actually remembering it only in part!


没有经验的学生经常创建可以很容易分解成10个或更多简单子条目的大条目。尽管看起来条目的数量变多了，但是重复每个小条目的时间往往很小以至于好过可能付出的代价 (1) 一次又一次忘记一个复杂的条目 (2) 以非常短的时间间隔重复 (3) 实际上只记住了复杂条目的一部分

# Cloze deletion is easy and effective
完型填空简单且有效。

初学者可能发现很坚持最小知识原则，那就使用完型填空吧。如果你是高级用户，也可以使用完型填空。

# Use imagery
使用图像


one picture is worth a thousand words

A graphic representation of information is usually far less volatile

# Avoid sets

大脑不容易记住 sets

如果不得已要使用 sets，可以将其转换成 enumerations。后者是有序的，更容易被记住。

# Avoid enumerations

可以使用 Cloze deletion 取代 enumerations

