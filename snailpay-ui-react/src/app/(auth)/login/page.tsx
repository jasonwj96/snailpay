import styles from "./page.module.scss";
import LoginForm from "@/app/components/login_form";

export default function LoginPage() {
  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <LoginForm />
      </div>
    </div>
  );
}
