git stash 
git pull origin master
git pull github master
git stash pop
git add .
git commit -m "auto sync"
git push origin master
git push github master
