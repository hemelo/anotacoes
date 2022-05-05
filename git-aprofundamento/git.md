# Git

```shell
$git reset --hard HEAD~5 # Reset 5 commits
$git merge --squash HEAD@{1} # Group to 1 commit

$git add -A # Prints all new and updated files are staged

$git remote -v # Prints a list of remote repositories and their URLs

$git reset --soft HEAD^  # Sets HEAD to the previous commit and leaves changes to stage

$git bisect # To compare the buggy commit to an early commit that works as expected

$git diff-tree [commit] # Display a list of files added or modified in a specific commit

$git fetch --all
$git reset --hard origin/master # Overwriting local repository

$git commit --amend -m "An updated commit message" # Changing history

$git rm --cached file # File will be removed from the staging area and its changes no longer tracked

$git diff --cached # Show stage diff

$git checkout -b <nameOfBranch> # Create new branch from current and switch to

$git merge --abort # Stop the merge and restore to the pre-merge state

$git branch --merged # List the branches that have been merged into the currently checked-out branch

$git clean -f # Remove untracked files

$git push --force-with-lease # Forces push even with merge conflict

$git stash show -p stash@{[number]} # See details of a specific stash

$git show-ref --head # Finds the HEAD of the current branch

$git commit --dry-run # If you are afraid of commit
```

- How could you squash multiple commits together without using git merge --squash? **REBASE**
- In a situation where you have several commits for a single task, what is the most efficient way to restructure your commit history? **SQUASH**
- What Git workflow is used by teams that collaborate on a single branch and avoid creating long-lived development branches? **Trunk-Based Development**
- What is the difference between a soft reset (git reset --soft) and a hard reset (git reset –hard)? *A soft reset only changes the commit that HEAD points to, while a hard reset resets the index and working tree to match the specified commit, discarding any changes.*

* By default a push doesn't send tags to the remote repository
* The command git cherry-pick is typically used to introduce particular commits from one branch within a repository onto a different branch.
* Warning 'detached HEAD': Means you are not working on the most recent commit of a branch.
