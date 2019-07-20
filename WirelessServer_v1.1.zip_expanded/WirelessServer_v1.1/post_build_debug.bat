timeout 10
xcopy build\* build_svn /e /i /q /y
timeout 1
xcopy dist\* dist_svn /e /i /q /y
