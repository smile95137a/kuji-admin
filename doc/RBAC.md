RBAC (Role-Based Access Control)
==============================

The system uses RBAC for the Admin backend. Main concepts:

- Role: A named role such as `Admin`, `StoreOwner`, `StoreEditor`.
- Menu: A left navigation item which is associated with a role.
- admin_user <-> admin_user_role <-> role <-> role_menu <-> menu flow.

Default roles:
- `Admin`: can manage everything across the platform.
- `StoreOwner`: manages store, lotteries, orders, and can invite Store Editors.
- `StoreEditor`: content/editor for owner-managed store (product management, prizes management).

How it works:
1. Admin creates a Store Owner (admin_user with role StoreOwner) and assigns them to a store.
2. Store Owner logs into `admin` portal and may create Store Editors by adding admin_user with StoreEditor role.
3. The front-end left menu is driven by `menu` entries bound through `role_menu`.

Implementation notes:
- A user may have multiple roles through the `admin_user_role` table.
- The JWT token contains `subject` as either the `admin_user.username` or `user.email`.
- The `JwtAuthenticationFilter` uses the username to check whether the token belongs to an Admin (from `admin_user`) or a Frontend user (from `user` table) and sets authorities accordingly.

