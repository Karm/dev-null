# Lambda Serialization

I'm trying to show [programmatic](./programmatic) ObjectFilter approach (Programmatic) and reachability-metadata.json approach ([Declarative](./declarative)) here. I can't make it work with pure JSON reachability-metadata config and it seems to me that one has to use programmatic ObjectFilter to do this :-(.  I **don't want to** fall back to pre-reachability metadata legacy JSON that allowed this, e.g. see [legacy](./legacy)) dir.


AI generated spreadsheet summary of this repo's sources and configs:

 Feature | Programmatic | Declarative | Legacy |
|---------|-------------|-------------|--------|
| **Status** | ✅ Works | ❌ Fails | ✅ Works |
| **Config File** | `reachability-metadata.json` | `reachability-metadata.json` | `serialization-config.json` |
| **Schema Version** | v1.2.0+ | v1.2.0+ | v1.1.0 |
| **ObjectInputFilter** | Required | Not used | Not required |
| **Build-time Interception** | Yes (invocation plugin) | No | Yes (config parsing) |
| **Registers** | Capturing class method | Specific lambda instance | Capturing class |
| **Runtime Flexibility** | High (wildcard matching) | None (fixed hash) | High (all lambdas in class) |
| **Recommended** | ✅ Yes | ❌ No | ⚠️ Legacy only |

### Test Programmatic (Works)
```bash
cd programmatic
./run.sh
```

### Test Declarative (Fails)
```bash
cd declarative
./run.sh
```

### Test Legacy (Works)
```bash
cd legacy
./run.sh
```
