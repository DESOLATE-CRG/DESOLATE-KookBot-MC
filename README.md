# DESOLATE-KookBot-MC
一个用于对接Minecraft服务器的KOOK BOT  
* 基于 [`JKOOK`](https://github.com/SNWCreations/JKook)
---

### 如何使用？
1. 按照 [`KookBC`](https://github.com/SNWCreations/KookBC)介绍搭建框架
2. 将本插件放入 `plugins` 文件夹
3. 启动 KookBC
> api-version：0.52.1
---

### 功能
<details><summary><b>查询服务器信息</b></summary>

- 在 `config.yml` 中 `ServerIPAddress` 填写服务器IP `ServerPort`填写服务器端口
- 在KOOK频道内使用命令 `/ServerInfo` 查询服务器在线人数等信息
> 若KookBC和Minecraft服务器运行在同一设备上，则只需要更改 `ServerPort`
</details>

<details><summary><b>查询PVP数据</b></summary>

- 先绑定玩家的KOOK和Minecraft服务器账户才可操作
- 对接 [`PVPStats`](https://github.com/slipcor/PVPStats) 查询数据
> 需要Minecraft服务器内安装 `PVPStats` 插件并将数据文件夹路径填入 `config.yml` 的 `PVPStateYmlFilePath`
</details>

<details><summary><b>查询具体武器击杀信息</b></summary>

- 对接 [`KillInformation`](https://github.com/Do0oMores/DESOLATE_KillInformation) 查询数据
> 需要Minecraft服务器内安装 `KillInformation` 插件并将数据文件夹路径填入 `config.yml` 的 `ItemKillState`
</details>