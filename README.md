# git-pre-merge-tool
Tool for processing git pre merge and print merge conflicts to terminal

git 分支合并测试命令行工具，用于在正式合并分支之前，进行分支的预先合并测试，并获取所有冲突文件列表，冲突行commit 信息，基于 JGit 进行开发；

![ sample](https://ws3.sinaimg.cn/large/006tKfTcgy1g0ioxcje05j31070et0x2.jpg)



### 开发信息

* **开发环境**
  * Intellij IDEA 2018.3
  * JDK 1.8.0_191
  * Groovy 2.5.4
* **编译环境**

  * Gradle 4.10.2
* **运行环境**

  * JRE 1.8

* **已测试环境**

  * Mac OS X - 10.14
  * Ubuntu - 16.04
  * Window - 10

  

###  运行方式

可以直接使用提供的二进制编译脚本启动，提供 Unix 和 Window 可运行的 shell 脚本和 bat 脚本，该工具的启动参数使用xml配置文件，没错就是因为我懒….  
<a href="https://github.com/Al-assad/git-pre-merge-tool/releases">二进制包和启动脚本下载地址</a>

#### 1. 配置运行参数

启动参数使用位于发布 zip 包下 `bin/pre-merge`，`bin/pre-merge.bat` 同目录的 `conf.xml`，参数配置如下格式：

```xml
<conf>
    <!--clone的本地仓库临时储存目录-->
    <templateFilePath>./tempRepo</templateFilePath>
    
    <!--git远程仓库登录信息-->
    <username>yulinying@sample.com</username>
    <password>123456</password>

    <!--合并仓库分支信息-->
    <repositories>
        <repo>
            <remoteUrl>https://github.com/Al-assad/ask.git</remoteUrl>
            <baseBranch>merge_test_1</baseBranch>
            <targetBranch>release</targetBranch>
        </repo>
        <repo>
            <remoteUrl>>https://github.com/Al-assad/sample.git</remoteUrl>
            <baseBranch>merge_test_2</baseBranch>
            <targetBranch>release</targetBranch>
        </repo>
    </repositories>

</conf>
```

**标签说明：**

* `<templateFilePath>` 仓库clone的本地临时仓库路径，一般不需要修改；
* `<username>` 远程仓库登录用户名称；
* `<password>` 远程仓库用户登录密码；

* `<repositories>` 用于配置所有的预合并测试任务，可以包含多个 `<repo>`  标签；

* `<repo>` 对应一个预合并测试任务，包含一下子标签：

  * `<remoteUrl>` 远程仓库 url；
  * `<baseBranch>` 合并基本分支；
  * `<targetBranch>` 合并目标分支；

  如下配置，表示测试将 git 仓库 `https://github.com/Al-assad/ask.git`  的 `merge_test_1` 分支合并到  `release`  分支；

  ```xml
  <repo>
      <remoteUrl>https://github.com/Al-assad/ask.git</remoteUrl>
      <baseBranch>merge_test_1</baseBranch>
      <targetBranch>release</targetBranch>
  </repo>
  ```

#### 2. 执行脚本

命令行运行位于发布 zip 包下的启动脚本：

1）osx/linux 环境运行 `bin/pre-merge` 

```
> sh pre-merge
```

2）window 环境运行 `bin/pre-merge.bat` 

```
> call pre-merge.bat 
```






