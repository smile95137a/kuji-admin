@echo off
echo Fixing duplicate content in Mapper XML files...

REM Fix UserMapper.xml (keep first 673 lines)
powershell -Command "(Get-Content 'src\main\resources\mapper\UserMapper.xml' -TotalCount 673) | Set-Content 'src\main\resources\mapper\UserMapper_temp.xml'"
type src\main\resources\mapper\UserMapper_temp.xml > src\main\resources\mapper\UserMapper.xml
echo ^</mapper^>>> src\main\resources\mapper\UserMapper.xml
del src\main\resources\mapper\UserMapper_temp.xml

REM Fix LotteryPrizeMapper.xml (keep first 482 lines)
powershell -Command "(Get-Content 'src\main\resources\mapper\LotteryPrizeMapper.xml' -TotalCount 482) | Set-Content 'src\main\resources\mapper\LotteryPrizeMapper_temp.xml'"
type src\main\resources\mapper\LotteryPrizeMapper_temp.xml > src\main\resources\mapper\LotteryPrizeMapper.xml
echo ^</mapper^>>> src\main\resources\mapper\LotteryPrizeMapper.xml
del src\main\resources\mapper\LotteryPrizeMapper_temp.xml

REM Fix ConsumptionRecordMapper.xml (keep first 306 lines)
powershell -Command "(Get-Content 'src\main\resources\mapper\ConsumptionRecordMapper.xml' -TotalCount 306) | Set-Content 'src\main\resources\mapper\ConsumptionRecordMapper_temp.xml'"
type src\main\resources\mapper\ConsumptionRecordMapper_temp.xml > src\main\resources\mapper\ConsumptionRecordMapper.xml
echo ^</mapper^>>> src\main\resources\mapper\ConsumptionRecordMapper.xml
del src\main\resources\mapper\ConsumptionRecordMapper_temp.xml

REM Fix ContactInquiryMapper.xml (keep first 393 lines)
powershell -Command "(Get-Content 'src\main\resources\mapper\ContactInquiryMapper.xml' -TotalCount 393) | Set-Content 'src\main\resources\mapper\ContactInquiryMapper_temp.xml'"
type src\main\resources\mapper\ContactInquiryMapper_temp.xml > src\main\resources\mapper\ContactInquiryMapper.xml
echo ^</mapper^>>> src\main\resources\mapper\ContactInquiryMapper.xml
del src\main\resources\mapper\ContactInquiryMapper_temp.xml

echo Done! All Mapper XML files have been fixed.
pause
