# 🌙 Moon Lockpick

**Moon Lockpick** یک پلاگین Minecraft برای **Paper 1.20.4** است که یک مینی‌گیم Lockpicking به سرور اضافه می‌کند. بازیکن با استفاده از آیتم اختصاصی لاک‌پیک، ناحیه هدف متحرک را در BossBar دنبال می‌کند و با زمان‌بندی درست تلاش می‌کند پین‌های قفل را یکی‌یکی باز کند.

## ✨ قابلیت‌ها

- مینی‌گیم Lockpicking با BossBar
- قفل‌های قابل تعریف با شناسه، سختی و تعداد پین
- آیتم اختصاصی Lockpick با دوام قابل تنظیم
- کم‌شدن یک واحد دوام لاک‌پیک در هر شکست و شکستن آیتم پس از اتمام دوام
- ثبت تعداد تلاش‌ها و موفقیت‌ها در SQLite
- اجرای Command بعد از باز شدن موفق قفل، به‌صورت Player یا Console
- API برای استفاده پلاگین‌های دیگر
- Eventهای قابل شنیدن برای شروع و نتیجه Lockpicking
- پشتیبانی اختیاری از WorldGuard، ItemsAdder و Vault به‌عنوان soft dependency

## 🎮 نحوه استفاده

بازیکن باید یک Lockpick سالم را در دست اصلی داشته باشد. هنگام فعال بودن مینی‌گیم، با **Swap Hand** (کلید پیش‌فرض F) ورودی Lockpick ثبت می‌شود.

اگر ناحیه هدف را درست انتخاب کند، پین بعدی فعال می‌شود. با یک خطای ناموفق، تلاش شکست می‌خورد و یک واحد از دوام Lockpick کم می‌شود.

## 🛠️ دستورات

```text
/lockpick create <id> <difficulty 0-1> <pinCount> [player|console] [cmd...]
/lockpick remove <id>
/lockpick give <player> [amount]
/lockpick stats <id>
/lockpick test [id] [difficulty]
/lockpick reload
```

### ساخت یک قفل

```text
/lockpick create vault 0.5 4 console give %player% diamond 1
```

در این مثال، قفلی با شناسه `vault` و چهار پین ساخته می‌شود و پس از موفقیت، Command از طرف Console اجرا می‌شود. مقدار `%player%` با نام بازیکن جایگزین می‌شود.

برای اجرای Command از طرف خود بازیکن، به‌جای `console` از `player` استفاده کنید.

## ⚙️ تنظیمات

فایل `config.yml` شامل تنظیمات BossBar، سرعت حرکت، پیام‌ها، SQLite و Lockpick است.

مهم‌ترین گزینه‌ها:

```yaml
bossbar:
  title: "§eدر حال باز کردن قفل..."
  color: YELLOW
  style: SOLID

session:
  base-speed: 0.02
  speed-increase-per-pin: 0.006
  cooldown-seconds: 3

lockpick-item:
  material: TRIPWIRE_HOOK
  name: "&eلاک‌پیک"
  max-uses: 5
```

## 📦 نصب

1. پلاگین را Build کنید.
2. فایل JAR خروجی را داخل پوشه `plugins` سرور Paper قرار دهید.
3. سرور را اجرا یا Restart کنید.
4. برای دریافت Lockpick از دستور زیر استفاده کنید:

```text
/lockpick give <player> [amount]
```

## 🔨 Build

پروژه با Maven ساخته می‌شود و به Java 17 نیاز دارد:

```bash
mvn clean package
```

فایل خروجی در پوشه `target` قرار می‌گیرد.

## 📚 API

کلاس `LockpickAPI` امکان مدیریت Lockها و شروع Lockpicking را برای پلاگین‌های دیگر فراهم می‌کند.

متدهای اصلی:

- `startLockpick(...)`
- `isLockpicking(...)`
- `forceFail(...)`
- `registerLock(...)`
- `removeLock(...)`
- `getLock(...)`
- `getAllLocks()`

Eventهای API:

- `LockpickStartEvent`
- `LockpickResultEvent`

## 💾 ذخیره‌سازی

اطلاعات Lockها و آمار بازیکنان در SQLite ذخیره می‌شود. مسیر پیش‌فرض دیتابیس:

```text
plugins/MoonLockpick/lockpick.db
```

## 📁 ساختار پروژه

```text
lockpick-plugin/
├── pom.xml
└── src/main/
    ├── java/ir/mahan/lockpick/
    │   ├── LockpickPlugin.java
    │   ├── LockpickManager.java
    │   ├── LockpickSession.java
    │   ├── api/
    │   ├── commands/
    │   ├── item/
    │   ├── listeners/
    │   └── storage/
    └── resources/
        ├── plugin.yml
        └── config.yml
```

## 📋 نیازمندی‌ها

- Java 17+
- Paper 1.20.4 یا نسخه سازگار
- Maven 3.8+ برای Build از سورس

## 👤 سازنده

**Mahan Vafadaran**

---

⭐ اگر پروژه برایتان مفید بود، با Star کردن Repository از توسعه آن حمایت کنید.
