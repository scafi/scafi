## [0.4.0](https://github.com/scafi/scafi/compare/v0.3.3...v0.4.0) (2022-03-04)


### Features

* **core>stdlib:** broadcast along given gradient function ([324ec71](https://github.com/scafi/scafi/commit/324ec718de6decd0c490ddbe8cd843d66b97914f))


### Bug Fixes

* add test also for js platform ([676b5dc](https://github.com/scafi/scafi/commit/676b5dcf87e363875b6df4bcc6f60d55bf8f28ff))
* broadcastAlongGradient ([a51e46a](https://github.com/scafi/scafi/commit/a51e46a0be087c761a5c6b0852567df78b60c911))
* cyclic dep between scalaVersion and crossScalaVersions ([43a886c](https://github.com/scafi/scafi/commit/43a886c1cbbd043239e09ac3ac201f39dd6b91de))
* **gha-workflow:** avoid to launch release on PRs ([f1f93b8](https://github.com/scafi/scafi/commit/f1f93b852d9537b5a14005007c244ceedefb7c4e))
* increase simulation steps, fix fatJar ([70818fc](https://github.com/scafi/scafi/commit/70818fcb65bd50995c70c94b3e2b17e687e19c95))
* issue [#44](https://github.com/scafi/scafi/issues/44) changing the ScafiBridge shared object initialization. fix: QuadTree problem in multiple node insertion. fix: EmptyAction throws exception in Simulation ([cc9bb41](https://github.com/scafi/scafi/commit/cc9bb41858292dc1de170af4d8ae0f66bb5cf0ad))
* keep track time of execution in an exec method ([b0d072e](https://github.com/scafi/scafi/commit/b0d072e68c9982d0d197fff390c2d4994673e3dc))
* sbt build ([a7e0c16](https://github.com/scafi/scafi/commit/a7e0c168b4e34addf7f104d8a63a167c5dd0fefa))
* **scafi-commons:** solve the bound check problem ([73f9af1](https://github.com/scafi/scafi/commit/73f9af1785524d46ab65fd6e0db54ff8c8dc1769))
* **scafi-core:** solve partially the alignment problem in scala.js ([3fad1e2](https://github.com/scafi/scafi/commit/3fad1e211406876a944ae2e3f8ba7a04fb79d194))
* **scafi-simulator:** add a simulated world clock to tackle [#61](https://github.com/scafi/scafi/issues/61) ([5e77544](https://github.com/scafi/scafi/commit/5e775442f2b8406eb1ddc3d0694ebcc4312ef1b8))
* **scafi-simulator:** align exec methods ([1a6dc54](https://github.com/scafi/scafi/commit/1a6dc54bf5a2cb9fe9305ed735784b84484e259e))
* **scafi-simulator:** use toEpochMilli in sense timestamp ([248549c](https://github.com/scafi/scafi/commit/248549c702d5834524bef102d17f6b84900e0150))
* **scafi-tests:** solve problem in test processes ([e735525](https://github.com/scafi/scafi/commit/e7355258b1de3b7dbe844cf4638b67687de2038e))
* **scafi-tests:** supert merge hood first ([9f181d3](https://github.com/scafi/scafi/commit/9f181d3218cc82ec4be137d8d7dcc49e6022b7fd))
* simulator-gui-new problems with java resources bundle, change // with . ([11055ca](https://github.com/scafi/scafi/commit/11055ca75a0c2a14b2244f3885ef9e52b9607791))
* **spala:** postfix ops ([9ae86d9](https://github.com/scafi/scafi/commit/9ae86d9311b2d79ae87dde2c8e478a9e9fbb64fe))
* unidoc paths for doc publishing on surge ([1e03876](https://github.com/scafi/scafi/commit/1e038764dd00c53a5bae9b72d8d80b7f541c98f4))


### Refactoring

* add type on public expressions ([e48f483](https://github.com/scafi/scafi/commit/e48f4836d079bfe97c76eaf51f36cc567f061ce8))
* change file name ([69166bd](https://github.com/scafi/scafi/commit/69166bd3f9dc2bbf23f231568291617e8e4be8fd))
* **core:** adjust expressions to accomodate improvements suggested by scapegoat ([4757e05](https://github.com/scafi/scafi/commit/4757e05dfeb6f4758d6d5fb73a647832cbd569fe))
* **core:** adjust interface of RoundVM to accomodate improvements suggested by scapegoat ([00c397b](https://github.com/scafi/scafi/commit/00c397b20af883bc36b6d283ffd72d97c5bc9bc0))
* define Gradient for all the algorithms ([9d23142](https://github.com/scafi/scafi/commit/9d23142d76e92d8b03c021866368d0900f1c5ec3))
* fix scalastyle issues in commons ([f2f9998](https://github.com/scafi/scafi/commit/f2f999812d74dd435afe1e8bb1261131e6eeb641))
* fix scalastyle issues in simulator ([fa9f929](https://github.com/scafi/scafi/commit/fa9f9299fe0e7f9c8164488df8f75a4d837b8125))
* lagMetric recevied as input ([edf73cb](https://github.com/scafi/scafi/commit/edf73cbf8d2797a041ccafde810c996cf449b301))
* merged all tests regarding FieldUtils into one file ([5b98683](https://github.com/scafi/scafi/commit/5b98683e05ec8599bff750714b7204bd967647ef))
* move evaporation to TimeUtils from BlockT ([ed23209](https://github.com/scafi/scafi/commit/ed23209b1ba4c47c3e4c4f3bc8360b4c8fe5657b))
* reduce code repetition and change network name ([fdf3285](https://github.com/scafi/scafi/commit/fdf3285fcce23498c25f6e4c202658ef3be3f90b))
* remove tests regarding excpetions and add parenthesis in functional call ([d85de9e](https://github.com/scafi/scafi/commit/d85de9e5a14a2979dc873ca686b3b3c173dc72e9))
* remove uselss import and change methods name ([198374b](https://github.com/scafi/scafi/commit/198374bff78bbe48841ae64829fef000be3c9d6f))
* reorder sbt plugins into logical clusters ([10040ee](https://github.com/scafi/scafi/commit/10040ee7a172e37d46c7c6b5d24af3f8f636faa0))
* run scalaFix LeakingImplicitClassVal on scafi ([aeade31](https://github.com/scafi/scafi/commit/aeade317ec1093e95d6bea4eb774d0894d6e234d))
* run scalaFix ProcedureSyntax on scafi ([7e4c934](https://github.com/scafi/scafi/commit/7e4c934b2584f58ff22a27228a760d1148a20950))
* run scalaFix RemoveUnused on scafi-commons ([50c3b4d](https://github.com/scafi/scafi/commit/50c3b4de5df14916570c41c81950244fbaccd7f0))
* run scalaFix RemoveUnused on scafi-core ([48b65c7](https://github.com/scafi/scafi/commit/48b65c7e865b183f792a691a71df383234634553))
* run scalaFix RemoveUnused on scafi-demos ([12002d7](https://github.com/scafi/scafi/commit/12002d7fc9f2bbc98699efff720e699f3e481133))
* run scalaFix RemoveUnused on scafi-demos-new ([46bf6f9](https://github.com/scafi/scafi/commit/46bf6f9244602b66c77c2c88a908e3d24228e51f))
* run scalaFix RemoveUnused on scafi-new-simulator-gui ([dca1267](https://github.com/scafi/scafi/commit/dca12679da763664c510ea70a34a47b97ec065e5))
* run scalaFix RemoveUnused on scafi-simulator-gui ([f5b5f18](https://github.com/scafi/scafi/commit/f5b5f1887b28482ff92eb98352ec73bd5fefa3e9))
* run scalaFix RemoveUnused on spala ([e413253](https://github.com/scafi/scafi/commit/e4132538991f990db0c8b33a4536acdab893a644))
* scalastyle issue in core: plus and bracket rule ([587eb1e](https://github.com/scafi/scafi/commit/587eb1e8eefd0fefcffffe7736548f535869eb9c))
* uniformed interface, metric received as param ([42ae1c3](https://github.com/scafi/scafi/commit/42ae1c386d7fcd6a6c12348aa175937145d6067d))
* use case matching istead of tuple decomposition ([470e832](https://github.com/scafi/scafi/commit/470e832a7986f27b0d496f810ebe4750b5d22042))


### Tests

* **core:** fix tests by correctly referring to VMStatus ([b54c4ef](https://github.com/scafi/scafi/commit/b54c4ef3fde57ee733a96f76217e0ccf45728fdd))
* exclude some packages for coverage analysis ([607aa22](https://github.com/scafi/scafi/commit/607aa2202bf3d98cbd7795581564808ad7064186))
* **tests:** temporarily ignore tests on ULT gradient ([aaa64ff](https://github.com/scafi/scafi/commit/aaa64ff0e44fca8b803228ada5381d0286ce38cc))


### General maintenance

* add debug deploy ([9ab6380](https://github.com/scafi/scafi/commit/9ab638035f256cfb2b85f1009345c919d0396b7f))
* change deploy trigger name ([4dd774a](https://github.com/scafi/scafi/commit/4dd774a7fcf30df64bb5ef50248a23785584a94e))
* check if work with liberica jdk ([4a45b4c](https://github.com/scafi/scafi/commit/4a45b4cbdf58c029a65834e03f7a4bd91f4514d1))
* **ci:** fix workflow ([10715b5](https://github.com/scafi/scafi/commit/10715b558c2f1f20559d2ab707c58be2c602d229))
* codecov badge in README (for master) ([0cb8212](https://github.com/scafi/scafi/commit/0cb821249ca33c1f11b56b3e3e52dbf6689b8680))
* coverage + publish on codecov ([3867301](https://github.com/scafi/scafi/commit/38673012ed06299da253e7dd8d77bd81371e7c63))
* file LICENSE date and minors in README ([671367e](https://github.com/scafi/scafi/commit/671367e3970853de49cfca5765d53bee77c72f76))
* fix surge domain ([ff592a0](https://github.com/scafi/scafi/commit/ff592a07bf2d06df83db09a728e7aa692df293c3))
* fix surge domain ([a8e7174](https://github.com/scafi/scafi/commit/a8e7174d1663c2a9ec9914b6412c35595fe05313))
* ignore husky ([05efa1c](https://github.com/scafi/scafi/commit/05efa1c461585d923315c6f66ff6ad7da2efa041))
* **plugin:** remove sbt-pgp ([1196e42](https://github.com/scafi/scafi/commit/1196e42c82331f4d1e6b13f7d5a0ae4e8d815b12))
* publish docs on surge ([5431744](https://github.com/scafi/scafi/commit/5431744d20865d80176fef251f67ca69cb233f1c))
* remove leave only ci-release ([8d4e68d](https://github.com/scafi/scafi/commit/8d4e68d53973958fb118557313d2d35672b00768))
* remove test, fix CI ([d4a4044](https://github.com/scafi/scafi/commit/d4a404464c3195dd81ae3344665366052fb112ed))
* remove yml to test the deploy ([7fd7a10](https://github.com/scafi/scafi/commit/7fd7a104b19e246a4f4e5c7e3e92615d7ec08cf3))
* **repo:** fix broken gitmodules ([7676a2f](https://github.com/scafi/scafi/commit/7676a2fe7e01d5b5aa31cf1de16816b22a3d5221))
* **scafi-tests:** println => info ([c992157](https://github.com/scafi/scafi/commit/c9921574e292d54388da83eb2def950c995317a1))
* test env for deploy ([5914f2c](https://github.com/scafi/scafi/commit/5914f2cfb3e09b45e35379d88b61347473e06d35))
* update dependecies, update build.sbt ([3cefdab](https://github.com/scafi/scafi/commit/3cefdab6cb78b58c219561addbf0a9b89fab5de4))


### Style improvements

* **build:** add newline at the end of file ([b707597](https://github.com/scafi/scafi/commit/b707597d9b33a5a50ca064347365a707c33fadf8))
* **ci:** fix indentation ([b491894](https://github.com/scafi/scafi/commit/b491894314e82372f789b06e88a0331f60d155fc))
* **core,tests:** minor refactoring ([18b1312](https://github.com/scafi/scafi/commit/18b131212bf6e5026c702febc60b2d47bec36213))
* **core:** adjust some infos/warnings/errors by scapegoat ([269c6b7](https://github.com/scafi/scafi/commit/269c6b72a24c1b728209de2f56c9720f11b53bc7))
* **core:** rename stdlib components to avoid underscore ([6719215](https://github.com/scafi/scafi/commit/6719215a5b228808b0af6025bebfae4ee4944d40))
* do not check import grouping ([dc5fe1e](https://github.com/scafi/scafi/commit/dc5fe1ec81df69db8d322e09e63aa6979601c587))
* fix scalastyle issues for stdlib-ext ([c44ebb1](https://github.com/scafi/scafi/commit/c44ebb11f50b5ec875c8fa9c61a3d903fc4aaad1))
* fix scalastyle issues in core module ([d783ca9](https://github.com/scafi/scafi/commit/d783ca905ad4b688bd0f8b301fa4e6c33bf60a93))
* fix scalastyle issues in module 'spala' ([7215b89](https://github.com/scafi/scafi/commit/7215b89a0d6a593a1cf7b5969d58c9376489f469))
* **gha-workflow:** fix  style in workflow spec ([d3fb641](https://github.com/scafi/scafi/commit/d3fb641ef376c58a3d3e3eb5052d55754a4e1230))
* **scafi-simulator-gui-new:** pass with scalafmt to remove inconsistencies ([2c70acc](https://github.com/scafi/scafi/commit/2c70acc6f01b26138169f8d2f803cdc53ba88377))
* scalafix ExplicitResultTypes ([ce79c7b](https://github.com/scafi/scafi/commit/ce79c7bf342e2686bde035a45dcc05473600d25f))


### Build and continuous integration

* add infrastructure for git hooks generation ([2f8470a](https://github.com/scafi/scafi/commit/2f8470a9a8f905d509bb13edc46485579e81520d))
* control ci concurrency and prevent interwoven release runs ([21c4bb4](https://github.com/scafi/scafi/commit/21c4bb429bb18a4e8c3022871852be955fb068be))
* drop Java 8 ([b97fbac](https://github.com/scafi/scafi/commit/b97fbac2be6ce8ad38b3ad7b4b9310e93640059f))
* enable full-scale multiplatform testing ([32792ed](https://github.com/scafi/scafi/commit/32792ed7c0705ae4876cb4c05b4c00c778bc0968))
* fix jdk version for coverage ([963d256](https://github.com/scafi/scafi/commit/963d2560e585eaae17a31286ba673e1df1ce0ccd))
* fix MacOS runner name ([9de6db1](https://github.com/scafi/scafi/commit/9de6db1394c9d786fb83a6cc9bf06d9a3fc304ee))
* fix pointer to javafx ([2726f78](https://github.com/scafi/scafi/commit/2726f7822ada8feed1b669ea26b9fdffe2a3f06d))
* **hooks:** add plugin for git conventional commit checker hook ([134867e](https://github.com/scafi/scafi/commit/134867e501b0c8379ea68fb1e6996d71be31fe50))
* pin the version of codecov-action ([2c6aa47](https://github.com/scafi/scafi/commit/2c6aa47cb444f491c5cc74d8aa2a7acf6422db95))
* prioritize the latest pushes ([33a6c10](https://github.com/scafi/scafi/commit/33a6c10d3e3162a03b8348750ec6e9b40fd6d46f))
* **release:** fix broken config.tagFormat ([d0366a3](https://github.com/scafi/scafi/commit/d0366a31b94675113de87a61ba38e35ff886a920))
* **release:** prefix v to the tag name ([4bf7751](https://github.com/scafi/scafi/commit/4bf7751134fffef92c3ba6508cc0ff00e5dfa24a))
* **release:** setup semantic-release ([3e01f26](https://github.com/scafi/scafi/commit/3e01f26b5bac9a01a7abdbe0cbd0f8f01da96021))
* **release:** use the deployment token ([51173b3](https://github.com/scafi/scafi/commit/51173b312e586b56428f200e61466cce5faeaacc))
* run on all branches and PRs ([bb4c19a](https://github.com/scafi/scafi/commit/bb4c19a7a466cdcc72d7ec72a5d6f4d7f9cf4de9))
* set scapegoat version + use cpd plugin ([5ce3b55](https://github.com/scafi/scafi/commit/5ce3b55844c941375900e93e735b7c0a5e237f62))
* test on java LTSs ([367e499](https://github.com/scafi/scafi/commit/367e499b4900497b6e042f30700dd97df1ac49b1))
* track the JDK provider directly in the variable ([6e758be](https://github.com/scafi/scafi/commit/6e758bece066e32acfc6708a1c45ab5edb161e79))
* update codecov-action to v2 ([eae6739](https://github.com/scafi/scafi/commit/eae6739e857d2a6ac409165f9457614fb50dcc24))
* update sbt ([aa7fa85](https://github.com/scafi/scafi/commit/aa7fa853742c03bf5b5fd3006b3d81dbfaee47ce))
* upgrade sbt-scalajs to 1.8.0 ([8feffe5](https://github.com/scafi/scafi/commit/8feffe581d1fcec9a032223966c1192d211dbd3c))
* upgrade scoverage ([5796e25](https://github.com/scafi/scafi/commit/5796e253e1647fb29e8a83da9e0b5ade3c98f22e))
