package site.assad.githandle.git;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * git操作类
 *
 * @author Al-assad
 * @since 2019/2/24
 * created by Intellij-IDEA
 */
public class GitHandler {

    private final static transient Logger LOGGER = LoggerFactory.getLogger(GitHandler.class);
    private GitConfVO confVO;

    public GitHandler(@Nonnull GitConfVO confVO) {
        this.confVO = confVO;
    }

    /**
     * clone仓库并进行预合并
     *
     * @param remoteUrl
     * @param baseBranch
     * @param targetBranch
     */
    public void preMerge(@Nonnull final String remoteUrl, @Nonnull final String baseBranch, @Nonnull final String targetBranch) throws IOException {
        File localPath = GitUtil.createFile(remoteUrl, confVO.getTempFilePath());
        Git git = null;
        try {
            git = gitClone(remoteUrl, localPath);
        } catch (GitAPIException e) {
            LOGGER.error(e.getMessage());
            System.out.println("Clone仓库失败！");
            //清除本地仓库
            FileUtils.deleteDirectory(localPath);
        }
        try {
            checkout(git, baseBranch);
            checkout(git, targetBranch);
            //合并分支
            MergeResult mergeResult = git.merge().
                    include(git.getRepository().resolve(baseBranch)).
                    setCommit(false).
                    setFastForward(MergeCommand.FastForwardMode.NO_FF).
                    setSquash(false).
                    call();
            //重新设 HEAD
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef("HEAD").call();
            printMergeResult(git, mergeResult);
        }  catch (GitAPIException e) {
            LOGGER.error(e.getMessage());
            System.out.println("预合并任务失败");
        } finally {
            //清除本地仓库
            FileUtils.deleteDirectory(localPath);
        }
    }

    /**
     * clone仓库
     */
    private Git gitClone(@Nonnull String remoteUrl, @Nonnull File localPath) throws GitAPIException {
        System.out.println(("Cloning from " + remoteUrl + " to " + localPath));
        Git git = Git
                .cloneRepository()
                .setURI(remoteUrl)
                .setCredentialsProvider(confVO.getCredentialsProvider())
                .setDirectory(localPath)
                .setCloneAllBranches(true)
                .call();
        System.out.println("Having repository: " + localPath);
        return git;
    }

    /**
     * checkout分支
     */
    private void checkout(@Nonnull Git git, @Nonnull String branchName) throws GitAPIException, IOException {
        git.checkout()
                .setCreateBranch(true)
                .setName(branchName)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .setStartPoint(GitUtil.getRemoteBranchName(branchName))
                .call();
        LOGGER.debug("checkout " + git.getRepository().getBranch());
    }

    /**
     * 打印合并结果
     *
     * @param mergeResult
     */
    public void printMergeResult(@Nonnull Git git, MergeResult mergeResult) throws GitAPIException {
        System.out.println("===================== Merge Report ======================");
        if (mergeResult == null) {
            System.out.println("No merge report");
            return;
        }
        System.out.println("Successful: " + mergeResult.getMergeStatus().isSuccessful());
        System.out.println("Merge Status: " + mergeResult.getMergeStatus().toString());
        if (mergeResult.getMergeStatus().isSuccessful()) {
            System.out.println("==========================================================");
            return;
        }

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Set<RevCommit> commitSet = new HashSet<>();
        System.out.println("Conflict Detail:");
        //打印冲突详情
        for (Map.Entry<String, int[][]> entry : mergeResult.getConflicts().entrySet()) {
            String file = entry.getKey();
            List<Integer> conflitLines = new ArrayList<>(entry.getValue().length);
            for (int[] arr : entry.getValue()) {
                conflitLines.add(arr[1]);
            }

            System.out.println("\tConflict file: " + file);
            //获取冲突文件提交信息
            BlameResult result = git.blame().setFilePath(file)
                    .setTextComparator(RawTextComparator.WS_IGNORE_ALL)
                    .call();
            RawText rawText = result.getResultContents();
            for (Integer lineIndex : conflitLines) {
                PersonIdent author = result.getSourceAuthor(lineIndex);
                RevCommit sourceCommit = result.getSourceCommit(lineIndex);
                commitSet.add(sourceCommit);
                System.out.println("\t\t"
                                + author.getName()
                                + (sourceCommit != null ? ": " + sourceCommit.getName() + ": " + sourceCommit.getShortMessage() + " [" + sf.format(author.getWhen()) + "] " : "")
                                + "->" + rawText.getString(lineIndex).trim());
            }
        }
        //打印简要commit冲突列表
        if (CollectionUtils.isNotEmpty(commitSet)) {
            System.out.println("Conflict Commit List:");
            commitSet.forEach(commit -> {
                System.out.println(commit.getCommitterIdent().getName() + ": "
                        + commit.getName() + ": "
                        + commit.getShortMessage() + " ["
                        + sf.format(commit.getCommitterIdent().getWhen()) + "] ");
            });
        }
        System.out.println("==========================================================\n");
    }

}
