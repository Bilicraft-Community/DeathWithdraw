# DeathWithdraw
Bilicraft死亡惩罚插件

## 介绍
与常规的死亡插件不同，本插件不会把钱扔进虚空，而是打到Tax账户里。

## 惩罚计算方式
设 `threshold` 为 `2000`。  
则玩家的钱在2k以下时，本插件不会进行任何操作。  
而如果钱超过2000，则超过的部分分割为`spilt`份，并没收其中的1份，最多没收`max`。
