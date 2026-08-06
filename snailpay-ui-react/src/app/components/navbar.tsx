"use client";

import { useState, useRef, useEffect } from "react";
import { createPortal } from "react-dom";
import Link from "next/link";
import styles from "./navbar.module.scss";
import Image from "next/image";
import navItems from "@/app/data/nav.json";

function Dropdown({
  item,
  anchor,
}: {
  item: (typeof navItems)[0];
  anchor: HTMLDivElement | null;
}) {
  const [pos, setPos] = useState({ top: 0, left: 0 });

  useEffect(() => {
    if (anchor) {
      const rect = anchor.getBoundingClientRect();
      setPos({
        top: rect.bottom,
        left: rect.left + rect.width / 2,
      });
    }
  }, [anchor]);

  return createPortal(
    <div className={styles.dropdown} style={{ top: pos.top, left: pos.left }}>
      {item.dropdown.map((link) => (
        <Link key={link.href} href={link.href} className={styles.dropdownItem}>
          <span className={styles.dropdownLabel}>{link.label}</span>
          <span className={styles.dropdownDesc}>{link.description}</span>
        </Link>
      ))}
    </div>,
    document.body,
  );
}

export default function Navbar() {
  const [activeItem, setActiveItem] = useState<string | null>(null);
  const anchorEls = useRef<Record<string, HTMLDivElement | null>>({});

  return (
    <div
      className={styles.navbarContainer}
      onMouseLeave={() => setActiveItem(null)}
    >
      <Image
        className={styles.navbarLogo}
        width={50}
        height={50}
        src="/snailpay-logo-full.svg"
        alt="Navbar logo"
      />

      <div className={styles.navbarLinks}>
        {navItems.map((item) => (
          <div
            key={item.label}
            className={styles.navItem}
            ref={(el) => {
              anchorEls.current[item.label] = el;
            }}
            onMouseEnter={() => setActiveItem(item.label)}
          >
            <Link
              className={`${styles.navbarLink} ${activeItem === item.label ? styles.navbarLinkActive : ""}`}
              href={item.href}
            >
              {item.label}
              <Image
                src="/chevron-down.svg"
                width={12}
                height={12}
                alt=""
                className={`${styles.chevron} ${activeItem === item.label ? styles.chevronOpen : ""}`}
              />
            </Link>

            {activeItem === item.label && (
              <Dropdown item={item} anchor={anchorEls.current[item.label]} />
            )}
          </div>
        ))}
      </div>

      <div className={styles.actionButtons}>
        <Link href="/login" className={styles.btnLogIn}>
          <p>Log In</p>
        </Link>
        <Link href="/register" className={styles.btnSignUp}>
          <p>Sign Up</p>
        </Link>
      </div>
    </div>
  );
}
