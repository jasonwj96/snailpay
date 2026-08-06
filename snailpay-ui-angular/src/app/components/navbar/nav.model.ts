export interface NavLink {
  href: string;
  label: string;
  description: string;
}

export interface NavItem {
  label: string;
  href: string;
  dropdown: NavLink[];
}