import os
import re
from pathlib import Path

# 掃描路徑
controller_dir = Path(r"c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\admin")

# 結果儲存
results = {}

def extract_apis(file_path):
    """提取單一 Controller 的所有 API"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 提取 @RequestMapping 的基礎路徑
    base_path_match = re.search(r'@RequestMapping\("([^"]+)"\)', content)
    base_path = base_path_match.group(1) if base_path_match else ""
    
    # 提取所有方法
    methods = []
    
    # 匹配格式：@GetMapping("/path") 或 @PostMapping(value = "/path")
    patterns = [
        (r'@GetMapping\("([^"]+)"\)', 'GET'),
        (r'@GetMapping\(value\s*=\s*"([^"]+)"\)', 'GET'),
        (r'@PostMapping\("([^"]+)"\)', 'POST'),
        (r'@PostMapping\(value\s*=\s*"([^"]+)"\)', 'POST'),
        (r'@PutMapping\("([^"]+)"\)', 'PUT'),
        (r'@PutMapping\(value\s*=\s*"([^"]+)"\)', 'PUT'),
        (r'@DeleteMapping\("([^"]+)"\)', 'DELETE'),
        (r'@DeleteMapping\(value\s*=\s*"([^"]+)"\)', 'DELETE'),
    ]
    
    for pattern, http_method in patterns:
        matches = re.finditer(pattern, content)
        for match in matches:
            path = match.group(1)
            full_path = base_path + path if path else base_path
            
            # 提取方法名（下一行通常是 public 方法）
            method_start = match.end()
            next_lines = content[method_start:method_start+500]
            method_name_match = re.search(r'public\s+\w+.*?(\w+)\s*\(', next_lines)
            method_name = method_name_match.group(1) if method_name_match else "unknown"
            
            methods.append({
                'http_method': http_method,
                'path': full_path,
                'method_name': method_name
            })
    
    return {
        'base_path': base_path,
        'methods': methods
    }

# 掃描所有 Controller
print("開始掃描後台 API...")
print("=" * 80)

for file_path in sorted(controller_dir.glob("*.java")):
    controller_name = file_path.stem
    print(f"\n📁 {controller_name}")
    
    try:
        api_info = extract_apis(file_path)
        results[controller_name] = api_info
        
        print(f"   基礎路徑: {api_info['base_path']}")
        print(f"   API 數量: {len(api_info['methods'])}")
        
        for method in api_info['methods']:
            print(f"   ✅ {method['http_method']:6} {method['path']:50} ({method['method_name']})")
    
    except Exception as e:
        print(f"   ❌ 錯誤: {str(e)}")

# 統計
print("\n" + "=" * 80)
print("📊 統計結果")
print("=" * 80)

total_apis = sum(len(info['methods']) for info in results.values())
print(f"Controller 數量: {len(results)}")
print(f"API 總數: {total_apis}")

print("\n各 Controller API 數量：")
for controller_name, info in sorted(results.items(), key=lambda x: len(x[1]['methods']), reverse=True):
    print(f"  {controller_name:40} {len(info['methods']):3} 個 API")
