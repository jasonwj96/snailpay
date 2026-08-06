import styles from "./login_form.module.scss";
import Image from "next/image";

export default function LoginForm() {
  return (
    <div>
      <div className={styles.logo}>
        <Image
          src="/snailpay-logo-full.svg"
          width={175}
          height={150}
          alt="Snailpay logo"
        />
      </div>
      <div className={styles.header}>
        <h1 className={styles.title}>Welcome back</h1>
        <p className={styles.subtitle}>
          Don&apos;t have an account? <a href="/register">Sign up</a>
        </p>
      </div>
      <form className={styles.form}>
        <div className={styles.field}>
          <label className={styles.label}>Email</label>
          <input
            className={styles.input}
            type="email"
            placeholder="jason@cloudbank.dev"
          />
        </div>

        <div className={styles.field}>
          <div className={styles.labelRow}>
            <label className={styles.label}>Password</label>
            <a className={styles.forgot} href="/forgot-password">
              Forgot password?
            </a>
          </div>
          <input
            className={styles.input}
            type="password"
            placeholder="••••••••"
          />
        </div>

        <button className={styles.submit} type="submit">
          Log in
        </button>
        <p className={styles.terms}>
          By logging in you agree to our <a href="/terms">Terms</a> and
          <a href="/privacy"> Privacy Policy</a>
        </p>
      </form>
    </div>
  );
}
