import styles from "./page.module.scss";
import SignupForm from "@/app/components/signup_form";

export default function RegisterPage() {
  return (
    <div className={styles.page}>
      <SignupForm />
    </div>
  );
}