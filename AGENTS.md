<div dir="rtl">

# AGENTS.md — فهرست عامل‌ها

> «چه کسی هست» اینجاست. برای «چطور کار می‌کنیم» به `AGENCY.md` و برای
> «در پروژه چطور اجرا می‌شود» به `workflows/` بروید.

## این فایل چگونه ساخته می‌شود

فهرست زیر **تولیدشده** است: از `agents/registry.tsv` خوانده و در
`AGENTS.md` نوشته می‌شود. آن را دستی ویرایش نکنید.

```bash
python3 scripts/gen-roster.py         # بازتولید فهرست
python3 scripts/gen-roster.py --check # تأیید تازه بودن (در CI)
```

هر مقدار ستون «شناسهٔ نصب» همان نام فایلی است که در `agents/<بخش>/` و در
`.claude/agents/` قرار می‌گیرد. برای فراخوانی در Claude Code از نام داخل
فرانت‌متر (`name:`) استفاده کنید:

```
از Frontend Developer استفاده کن و صفحهٔ ورود را بساز.
از Reality Checker بخواه این تغییر را قبل از انتشار تأیید کند.
```

## چطور یک عامل را فراخوانی کنیم

| می‌خواهم... | چه کسی | چطور صدا بزنم |
| --- | --- | --- |
| یک جریان کامل را اجرا کنم | `agents-orchestrator` | «Agents Orchestrator را در حالت Sprint فعال کن…» |
| یک تسک را بشکنم و اولویت بدهم | `sprint-prioritizer` | «این اسپک را به تسک‌های یک‌هفته‌ای بشکن.» |
| معماری را تعیین کنم | `software-architect` | «مرزهای سرویس و ریسک‌های این تغییر را طراحی کن.» |
| کد امن بنویسم | `appsec-engineer` | «این دیف را از منظر حمله مرور کن.» |
| قبل از انتشار مطمئن شوم | `reality-checker` | «شواهد کافی برای انتشار هست؟ پیش‌فرضت «نه» باشد.» |

قاعدهٔ ارجاع: **همیشه شناسهٔ انگلیسی عامل را بگویید**، نه ترجمهٔ فارسی. نام‌های
فارسی برای گفت‌وگوی انسانی‌اند؛ موتور عامل را با شناسهٔ انگلیسی صدا می‌زند.

## فهرست کامل عامل‌ها

<!-- BEGIN:ROSTER -->
مجموع: **۴۸** عامل در **۶** بخش. همهٔ متن‌های عاملی از پروژهٔ بالادست آمده‌اند و بازنویسی نشده‌اند.

| بخش | عامل | شناسهٔ نصب | کی فعالش کنیم |
| --- | --- | --- | --- |
| [اجرایی (Executive)](#executive) | 🎛️ Agents Orchestrator | `agents-orchestrator` | اجرای یکپارچهٔ یک جریان کاری کامل از اسپک تا انتشار |
|  | ♟️ Business Strategist | `business-strategist` | تحلیل رقبا، ورود به بازار، مدل درآمدی، و تصمیم‌های استراتژیک |
|  | 🧭 Chief of Staff | `chief-of-staff` | واگذاری چندبخشی، هم‌ترازی خروجی‌ها، و تصمیم‌هایی که بین تیم‌ها گم می‌شوند |
|  | 🧭 Product Manager | `product-manager` | مالکیت چرخهٔ عمر محصول از کشف نیاز تا GTM و سنجش نتیجه |
|  | 📝 Senior Project Manager | `senior-project-manager` | شکستن اسپک به تسک، برآورد دامنه، و پیگیری اجرا |
|  | 🎯 Sprint Prioritizer | `sprint-prioritizer` | اولویت‌بندی بک‌لاگ، برنامه‌ریزی اسپرینت، و تخصیص ظرفیت |
| [مهندسی (Engineering)](#engineering) | 🤖 AI Engineer | `ai-engineer` | ادغام مدل، RAG، و جریان‌های هوش مصنوعی در محصول |
|  | 🔌 API Platform Engineer | `api-platform-engineer` | پلتفرم API، قراردادها، نسخه‌بندی، و gateway |
|  | 🏗️ Backend Architect | `backend-architect` | طراحی API، مدل داده، و مقیاس‌پذیری سمت سرور |
|  | 🔧 Data Engineer | `data-engineer` | خطوط داده، انبار داده، و کیفیت داده |
|  | ⚙️ DevOps Automator | `devops-automator` | CI/CD، اتوماسیون زیرساخت، و استقرار |
|  | 🖥️ Frontend Developer | `frontend-developer` | پیاده‌سازی رابط، کامپوننت‌ها، و کارایی رندر |
|  | 🌿 Git Workflow Master | `git-workflow-master` | استراتژی شاخه، کامیت متعارف، و تاریخ تمیز |
|  | 🚨 Incident Response Commander | `incident-response-commander` | فرمان‌دهی به حادثهٔ عملیاتی و بازبینی پس از حادثه |
|  | 📲 Mobile App Builder | `mobile-app-builder` | اپ موبایل بومی یا چندسکویی |
|  | 🛤️ Platform Engineer | `platform-engineer` | پلتفرم توسعه‌دهنده و مسیر طلایی تحویل |
|  | ⚡ Rapid Prototyper | `rapid-prototyper` | نمونهٔ اولیهٔ سریع برای اعتبارسنجی ایده |
|  | 💎 Senior Developer | `senior-developer` | مسائل سخت پیاده‌سازی و بازآرایی ساختاری |
|  | 🏛️ Software Architect | `software-architect` | معماری کلان، مرز سرویس‌ها، و تصمیم‌های غیرقابل‌برگشت |
|  | 🛡️ SRE (Site Reliability Engineer) | `sre` | SLO، هشداردهی، و قابلیت اطمینان عملیاتی |
|  | 📚 Technical Writer | `technical-writer` | مستندات توسعه‌دهنده، مرجع API، و راهنما |
| [طراحی و محصول (Design)](#design) | 🎨 Brand Guardian | `brand-guardian` | یکپارچگی هویت برند در همهٔ نقاط تماس |
|  | 🔍 Feedback Synthesizer | `feedback-synthesizer` | تبدیل بازخورد پراکنده به اولویت قابل اجرا |
|  | 📷 Image Prompt Engineer | `image-prompt-engineer` | پرامپت تصویر برای دارایی‌های برند |
|  | 🔭 Trend Researcher | `trend-researcher` | رصد روند بازار و فرصت‌های نوظهور |
|  | 🎨 UI Designer | `ui-designer` | طراحی بصری، سیستم طراحی، و جزئیات تعامل |
|  | 📐 UX Architect | `ux-architect` | معماری اطلاعات، جریان کاربر، و اسکلت تجربه |
|  | 🔬 UX Researcher | `ux-researcher` | مصاحبه، آزمون کاربر، و شواهد رفتاری |
|  | 🎬 Visual Storyteller | `visual-storyteller` | روایت بصری، داستان محصول، و ارائه |
| [امنیت (Security)](#security) | 🔎 AI-Generated Code Security Auditor | `ai-generated-code-auditor` | بازبینی کد تولیدشده توسط مدل پیش از ادغام |
|  | 🔐 Application Security Engineer | `appsec-engineer` | بازبینی امن کد و کنترل‌های AppSec |
|  | ☁️ Cloud Security Architect | `cloud-security-architect` | امنیت ابر، IAM، و جداسازی شبکه |
|  | 📋 Compliance Auditor | `compliance-auditor` | انطباق، سیاست‌ها، و شواهد حسابرسی |
|  | 🚨 Incident Responder | `incident-responder` | مهار، ریشه‌یابی، و پاک‌سازی رخداد امنیتی |
|  | 🗡️ Penetration Tester | `penetration-tester` | آزمون نفوذ و اثبات اکسپلویت |
|  | 🔑 Secrets & Credential Hygiene Engineer | `secrets-credential-hygiene-engineer` | چرخهٔ عمر رازها، چرخش کلید، و پاسخ به افشا |
|  | 🛡️ Security Architect | `security-architect` | معماری امن، مدل تهدید، و مرزهای اعتماد |
| [کیفیت (Quality)](#quality) | ♿ Accessibility Auditor | `accessibility-auditor` | انطباق دسترس‌پذیری و WCAG |
|  | 🔌 API Tester | `api-tester` | آزمون قرارداد، مرز، و رفتار API |
|  | 👁️ Code Reviewer | `code-reviewer` | بازبینی کد برای نگه‌داشت‌پذیری و ریسک |
|  | ⏱️ Performance Benchmarker | `performance-benchmarker` | سنجش کارایی، بار، و رگرسیون عملکرد |
|  | 🧐 Reality Checker | `reality-checker` | دروازهٔ پذیرش مبتنی بر شاهد؛ پیش‌فرض «نیاز به کار» |
|  | 🎭 Test Automation Engineer | `test-automation-engineer` | طراحی هرم آزمون و اتوماسیون تست |
| [بازاریابی و رشد (Marketing)](#marketing) | ✍️ Content Creator | `content-creator` | تولید محتوای هدفمند و تقویم محتوا |
|  | 🚀 Growth Hacker | `growth-hacker` | آزمایش رشد، قیف جذب، و حلقهٔ بازگشت |
|  | 📣 PR & Communications Manager | `pr-communications-manager` | روابط عمومی، پیام‌رسانی بحران، و بیانیه‌ها |
|  | 🔍 SEO Specialist | `seo-specialist` | جست‌وجوی ارگانیک و ساختار محتوای سئو |
|  | 📣 Social Media Strategist | `social-media-strategist` | استراتژی شبکه‌های اجتماعی و تقویم انتشار |

<a id="executive"></a>

### اجرایی (Executive) — ۶ عامل

| عامل | فایل منبع | شناسهٔ Claude Code |
| --- | --- | --- |
| 🎛️ **Agents Orchestrator** | `agents-orchestrator.md` | `agents-orchestrator` |
| ♟️ **Business Strategist** | `business-strategist.md` | `business-strategist` |
| 🧭 **Chief of Staff** | `specialized-chief-of-staff.md` | `specialized-chief-of-staff` |
| 🧭 **Product Manager** | `product-manager.md` | `product-manager` |
| 📝 **Senior Project Manager** | `project-manager-senior.md` | `project-manager-senior` |
| 🎯 **Sprint Prioritizer** | `product-sprint-prioritizer.md` | `product-sprint-prioritizer` |

<a id="engineering"></a>

### مهندسی (Engineering) — ۱۵ عامل

| عامل | فایل منبع | شناسهٔ Claude Code |
| --- | --- | --- |
| 🤖 **AI Engineer** | `engineering-ai-engineer.md` | `engineering-ai-engineer` |
| 🔌 **API Platform Engineer** | `engineering-api-platform-engineer.md` | `engineering-api-platform-engineer` |
| 🏗️ **Backend Architect** | `engineering-backend-architect.md` | `engineering-backend-architect` |
| 🔧 **Data Engineer** | `engineering-data-engineer.md` | `engineering-data-engineer` |
| ⚙️ **DevOps Automator** | `engineering-devops-automator.md` | `engineering-devops-automator` |
| 🖥️ **Frontend Developer** | `engineering-frontend-developer.md` | `engineering-frontend-developer` |
| 🌿 **Git Workflow Master** | `engineering-git-workflow-master.md` | `engineering-git-workflow-master` |
| 🚨 **Incident Response Commander** | `engineering-incident-response-commander.md` | `engineering-incident-response-commander` |
| 📲 **Mobile App Builder** | `engineering-mobile-app-builder.md` | `engineering-mobile-app-builder` |
| 🛤️ **Platform Engineer** | `engineering-platform-engineer.md` | `engineering-platform-engineer` |
| ⚡ **Rapid Prototyper** | `engineering-rapid-prototyper.md` | `engineering-rapid-prototyper` |
| 💎 **Senior Developer** | `engineering-senior-developer.md` | `engineering-senior-developer` |
| 🏛️ **Software Architect** | `engineering-software-architect.md` | `engineering-software-architect` |
| 🛡️ **SRE (Site Reliability Engineer)** | `engineering-sre.md` | `engineering-sre` |
| 📚 **Technical Writer** | `engineering-technical-writer.md` | `engineering-technical-writer` |

<a id="design"></a>

### طراحی و محصول (Design) — ۸ عامل

| عامل | فایل منبع | شناسهٔ Claude Code |
| --- | --- | --- |
| 🎨 **Brand Guardian** | `design-brand-guardian.md` | `design-brand-guardian` |
| 🔍 **Feedback Synthesizer** | `product-feedback-synthesizer.md` | `product-feedback-synthesizer` |
| 📷 **Image Prompt Engineer** | `design-image-prompt-engineer.md` | `design-image-prompt-engineer` |
| 🔭 **Trend Researcher** | `product-trend-researcher.md` | `product-trend-researcher` |
| 🎨 **UI Designer** | `design-ui-designer.md` | `design-ui-designer` |
| 📐 **UX Architect** | `design-ux-architect.md` | `design-ux-architect` |
| 🔬 **UX Researcher** | `design-ux-researcher.md` | `design-ux-researcher` |
| 🎬 **Visual Storyteller** | `design-visual-storyteller.md` | `design-visual-storyteller` |

<a id="security"></a>

### امنیت (Security) — ۸ عامل

| عامل | فایل منبع | شناسهٔ Claude Code |
| --- | --- | --- |
| 🔎 **AI-Generated Code Security Auditor** | `security-ai-generated-code-auditor.md` | `security-ai-generated-code-auditor` |
| 🔐 **Application Security Engineer** | `security-appsec-engineer.md` | `security-appsec-engineer` |
| ☁️ **Cloud Security Architect** | `security-cloud-security-architect.md` | `security-cloud-security-architect` |
| 📋 **Compliance Auditor** | `security-compliance-auditor.md` | `security-compliance-auditor` |
| 🚨 **Incident Responder** | `security-incident-responder.md` | `security-incident-responder` |
| 🗡️ **Penetration Tester** | `security-penetration-tester.md` | `security-penetration-tester` |
| 🔑 **Secrets & Credential Hygiene Engineer** | `security-secrets-credential-engineer.md` | `security-secrets-credential-engineer` |
| 🛡️ **Security Architect** | `security-architect.md` | `security-architect` |

<a id="quality"></a>

### کیفیت (Quality) — ۶ عامل

| عامل | فایل منبع | شناسهٔ Claude Code |
| --- | --- | --- |
| ♿ **Accessibility Auditor** | `testing-accessibility-auditor.md` | `testing-accessibility-auditor` |
| 🔌 **API Tester** | `testing-api-tester.md` | `testing-api-tester` |
| 👁️ **Code Reviewer** | `engineering-code-reviewer.md` | `engineering-code-reviewer` |
| ⏱️ **Performance Benchmarker** | `testing-performance-benchmarker.md` | `testing-performance-benchmarker` |
| 🧐 **Reality Checker** | `testing-reality-checker.md` | `testing-reality-checker` |
| 🎭 **Test Automation Engineer** | `testing-test-automation-engineer.md` | `testing-test-automation-engineer` |

<a id="marketing"></a>

### بازاریابی و رشد (Marketing) — ۵ عامل

| عامل | فایل منبع | شناسهٔ Claude Code |
| --- | --- | --- |
| ✍️ **Content Creator** | `marketing-content-creator.md` | `marketing-content-creator` |
| 🚀 **Growth Hacker** | `marketing-growth-hacker.md` | `marketing-growth-hacker` |
| 📣 **PR & Communications Manager** | `marketing-pr-communications-manager.md` | `marketing-pr-communications-manager` |
| 🔍 **SEO Specialist** | `marketing-seo-specialist.md` | `marketing-seo-specialist` |
| 📣 **Social Media Strategist** | `marketing-social-media-strategist.md` | `marketing-social-media-strategist` |

<!-- END:ROSTER -->

## نقشهٔ شناسه → فایل

| شناسه | فایل متن | فایل نصب‌شده |
| --- | --- | --- |
| `<upstream-stem>` | `agents/<بخش>/<slug>.md` | `.claude/agents/<upstream-stem>.md` |

تفاوت `<slug>` و `<upstream-stem>` عمدی است: نام فایل در `agents/` کوتاه و
تمیز است (`sre`)، نام نصب‌شده همان شناسهٔ بالادست است (`engineering-sre`) تا
همگام‌سازی بعدی و نصب ابزارهای دیگر بدون نقشه‌برداری دستی کار کند. جدول کامل
نگاشت در `.claude/AGENCY-ROSTER.md` (تولیدشده) و ستون‌های `registry.tsv` است.

## چیزهایی که این فهرست نیست

- **فهرست همهٔ ۲۸۳ عامل بالادست نیست.** ۴۸ عامل منتخب است. برای دیدن بقیه:
  ```bash
  upstream/agency-agents/scripts/install.sh --list agents
  ```
- **جایگزین `strategy/NEXUS` نیست.** اگر به‌جای یک عامل منتخب، دکترین کامل
  چندفازی می‌خواهید، `upstream/agency-agents/strategy/QUICKSTART.md` را بخوانید.
- **ادعای خودکاربودن نیست.** هر عامل باید صریح فراخوانی شود؛ هیچ حلقهٔ
  پس‌زمینه‌ای اجرا نمی‌شود.

</div>
