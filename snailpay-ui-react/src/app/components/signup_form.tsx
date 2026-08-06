import styles from "./signup_form.module.scss";
import Image from "next/image";

export default function SignupForm() {
  return (
    <div className={styles.wrapper}>
      <div className={styles.left}>
        <div className={styles.logo}>
          <Image
            src="/snailpay-logo-full.svg"
            width={175}
            height={150}
            loading="eager"
            alt="Snailpay logo"
            placeholder="blur"
            blurDataURL="..."
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          />
        </div>
        <div className={styles.header}>
          <h1 className={styles.title}>Create your Snailpay account</h1>
          <p className={styles.subtitle}>
            Already have an account? <a href="/login">Log in</a>
          </p>
        </div>
        <form className={styles.form}>
          <div className={styles.field}>
            <label className={styles.label}>Full name</label>
            <input
              className={styles.input}
              type="text"
              name="Full name"
              placeholder="John Doe"
            />
          </div>
          <div className={styles.field}>
            <label className={styles.label}>Email</label>
            <input
              className={styles.input}
              type="email"
              name="Email"
              placeholder="user@snailpay.com"
            />
          </div>
          <div className={styles.field}>
            <label className={styles.label}>Password</label>
            <input
              className={styles.input}
              type="password"
              name="Password"
              placeholder="************"
            />
          </div>
          <button className={styles.submit} type="submit">
            Create account
          </button>
          <p className={styles.terms}>
            By signing up you agree to our <a href="/terms">Terms</a> and
            <a href="/privacy"> Privacy Policy</a>
          </p>
        </form>
      </div>
      <div className={styles.right}>
        <Image
          src="/signup-illustration.png"
          alt="Snailpay visual"
          fill
          className={styles.image}
        />
      </div>
    </div>
  );
}
