"""
自動為所有 Entity 類別加上 Lombok 註解
"""
import os
import re

# Entity 資料夾路徑
entity_dir = r"c:\Users\user\OneDrive\Desktop\創業\KUJI-Server\admin\src\main\java\com\group\admin\entity"

# Lombok 註解模板
lombok_imports = """import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;"""

lombok_annotations = """@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor"""

def add_lombok_to_file(file_path):
    """為單一檔案加上 Lombok 註解"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 如果已經有 @Data，跳過
    if '@Data' in content:
        print(f"✓ {os.path.basename(file_path)} 已經有 Lombok 註解")
        return
    
    # 找到 package 聲明
    package_match = re.search(r'package\s+[\w.]+;', content)
    if not package_match:
        print(f"✗ {os.path.basename(file_path)} 找不到 package 聲明")
        return
    
    package_end = package_match.end()
    
    # 移除所有現有的 import（如果有）
    content_after_package = content[package_end:]
    class_match = re.search(r'public\s+class\s+\w+', content_after_package)
    
    if not class_match:
        print(f"✗ {os.path.basename(file_path)} 找不到 class 聲明")
        return
    
    # 重建檔案內容
    new_content = content[:package_end] + "\n\n"
    new_content += lombok_imports + "\n\n"
    new_content += lombok_annotations + "\n"
    new_content += content_after_package[class_match.start():]
    
    # 寫回檔案
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f"✓ {os.path.basename(file_path)} 已加上 Lombok 註解")

# 處理所有 .java 檔案
if os.path.exists(entity_dir):
    for filename in os.listdir(entity_dir):
        if filename.endswith('.java'):
            file_path = os.path.join(entity_dir, filename)
            add_lombok_to_file(file_path)
    print("\n✅ 所有 Entity 已處理完成！")
else:
    print(f"✗ 找不到資料夾: {entity_dir}")
