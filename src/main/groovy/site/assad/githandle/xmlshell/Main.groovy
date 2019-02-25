package site.assad.githandle.xmlshell

import org.apache.commons.lang3.StringUtils
import site.assad.githandle.git.GitConfVO
import site.assad.githandle.git.GitHandler

/**
 * 基于 xml 配置方式的 shell 交互
 *
 * @author Al-assad
 * @since 2019/2/24
 * created by Intellij-IDEA
 */
class Main {

    final static CONF_PATH = "./conf.xml"

    static printError = { def message ->
        println message
        System.exit(-1)
    }

    static def checkContents(String... contents) {
        for (def content : contents) {
            if (StringUtils.isEmpty(content))
                return false
        }
        return true
    }

    static void main(String[] args) {
        println "Git pre-merge task starting..."
        //获取配置参数
        def conf = raedConf()
        String tempPath = conf.templateFilePath.text()
        String username = conf.username.text()
        String password = conf.password.text()
        if (!checkContents(tempPath, username, password)) {
            printError("$CONF_PATH 中的配置参数有误")
        }
        def confVO = GitConfVO.of(username, password, tempPath)
        def repositories = conf.repositories
        if (!checkContents(repositories.text())) {
            printError("无预合并任务")
        }
        def repos = repositories.repo
        //运行预合并任务
        repos.each {
            String remoteUrl = it.remoteUrl.text()
            String baseBranch = it.baseBranch.text()
            String targetBranch = it.targetBranch.text()
            if (!checkContents(remoteUrl, baseBranch, targetBranch)) {
                printError("$CONF_PATH 中的仓库预合并任务配置参数有误")
            } else {
                def gitHandler = new GitHandler(confVO)
                gitHandler.preMerge(remoteUrl, baseBranch, targetBranch)
            }
        }
        println "Git pre-merge task complete."

    }

    //读取xml配置文件
    static def raedConf() {
        def parser = new XmlParser()
        def confFile = new File(CONF_PATH)
        if (!confFile.exists() || !confFile.isFile()) {
            println "配置文件conf.xml不存在"
            System.exit(-1)
        }
        def conf = parser.parse(confFile)
        return conf
    }


}
