@echo off
set GS_DEPLOYED=
set DP_DEPLOYED=
set EXIT_F=

rem ここを環境に合わせて変更してください。「C:\L2J」とかでもOK
set DEPLOY_DIR=..\..\staging

:deployed_start
echo L2Jmobius_jp_pvpのデプロイを開始します。

:gsdeployed_start
set /P GS_DEPLOYED="ゲームサーバをデプロイしますか？(y/n) "
if /i %GS_DEPLOYED%==y (goto gs_deployed_run) else (goto gs_deployed_end)

:gs_deployed_run
echo ゲームサーバのデプロイを開始します
robocopy ..\build\dist %DEPLOY_DIR%\ /s /xf General.properties > _gsdeploylog.txt
echo ゲームサーバのデプロイが完了しました

:gs_deployed_end

:dp_deployed_start
set /P DP_DEPLOYED="データパックをデプロイしますか？(y/n) "
if /i %DP_DEPLOYED%==y (goto dp_deployed_run) else (goto dp_deployed_end)

:dp_deployed_run
echo データパックのデプロイを開始します
robocopy dist %DEPLOY_DIR%\ /s > _dpdeploylog.txt

echo データパックのデプロイが完了しました

:dp_deployed_end
:exit_start
set /P EXIT_F="終了しますか？(y/n) "
if /i %EXIT_F%==y (goto exit_run) else (goto deployed_start)

:exit_run
echo 終了します。
pause
exit
