import React from 'react';
import DashboardCards from '../components/DashboardCards';
import { useTranslation } from '../context/LanguageContext';

const DashboardPage = () => {
  const { t } = useTranslation();
  
  return (
    <div className="space-y-8">
      <section className="rounded-[32px] bg-dashboard-gradient p-8 text-white shadow-2xl shadow-primary/20 md:p-10">
        <div className="max-w-3xl">
          <p className="text-sm font-semibold uppercase tracking-[0.3em] text-white/70">{t('dashboardPage.premiumLibraryAdmin')}</p>
          <h2 className="mt-4 text-4xl font-black tracking-tight md:text-5xl">{t('dashboardPage.operateDescription')}</h2>
          <p className="mt-5 max-w-2xl text-base text-white/85 md:text-lg">
            {t('dashboardPage.manageDescription')}
          </p>
        </div>
      </section>

      <DashboardCards />
    </div>
  );
};

export default DashboardPage;
