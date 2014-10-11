/** 
 * This class handles all database access needs with methods that query the database and return the results in a structed format
 * (ex. ArrayList)
 * 
 * @author Hannah Wilder (with code borrowed from http://blog.softeq.com/2012/12/using-pre-populated-sqlite-database-in.html)
 * @version 1.1
 */

package com.example.careerfair;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ListView;

public class DbAccess {

	private static final String DB_NAME = "careerFairDB.db";

	//A good practice is to define database field names as constants
	private static final String COMPANY_TABLE_NAME = "company";
	private static final String COMPANY_ID = "_id";
	private static final String COMPANY_NAME = "name";


	/**
	 * Queries the database to obtain a list of company names and fill an array list with them
	 * 
	 * @param companies - an empty array list to fill with company names
	 * @param database - the database to query (obtain database with ExternalDbOpenHelper instance)
	 */
	public static void fillCompanies(ArrayList companies, SQLiteDatabase database) {
		//companies = new ArrayList<String>();
		Cursor companiesCursor = database.query(COMPANY_TABLE_NAME, new String[] {COMPANY_ID,
				COMPANY_NAME}, null, null, null, null, COMPANY_NAME);
		companiesCursor.moveToFirst();
		if(!companiesCursor.isAfterLast()) {
			do {
				String name = companiesCursor.getString(1);
				companies.add(name);
			} while (companiesCursor.moveToNext());
		}
		companiesCursor.close();
	}
	
	/**
     * getAllCompanies - gets all the companies in the database
     * 
     * @param companies - an ArrayList to fill with companies
     * @param database - the name of the database to open (careerFairDB.db)
     */
	public static void getAllCompanies(ArrayList<Company> companies, SQLiteDatabase database) {
		Cursor companiesCursor = database.rawQuery("SELECT company.name, company.website, location.tableNum, room.name FROM company, companyToLocation, location, room WHERE company._id=companyToLocation.companyID AND companyToLocation.locationID=location._id AND location.roomID=room._id ORDER BY company.name;", new String[0]);
		companiesCursor.moveToFirst();
		if(!companiesCursor.isAfterLast()) {
			do {
				String name = companiesCursor.getString(0);
				String website = companiesCursor.getString(1);
				String tableNum = companiesCursor.getString(2);
				String room = companiesCursor.getString(3);
				
				Company newCompany = new Company(name, website, tableNum, room, getMajorsForCompany(name, database), getPositionsForCompany(name, database), getWorkAuthsForCompany(name, database));
				companies.add(newCompany);
				
				
			} while (companiesCursor.moveToNext());
		}
		companiesCursor.close();
	}
	
	/**
     * getAllCompanies - gets all the companies in the database
     * 
     * @param database - the name of the database to open (careerFairDB.db)
     * @return an ArrayList filled with companies
     */
	public static ArrayList<Company> getAllCompanies(SQLiteDatabase database) {
		ArrayList<Company> companies = new ArrayList<Company>();
		getAllCompanies(companies, database);
		return companies;
	}
	
	/**
     * getMajorsForCompany - gets all the majors a specific company is looking for
     * 
     * @param company - the name of the company to get the majors for
     * @param database - the name of the database to open (careerFairDB.db)
     * @return an ArrayList filled with majors
     */
	public static ArrayList<Major> getMajorsForCompany(String company, SQLiteDatabase database) {
		String[] nameArray = {company};
		
		ArrayList<Major> majors = new ArrayList<Major>();
		Cursor majorsCursor = database.rawQuery("SELECT major.name, major.abbreviation FROM company, companyToMajor, major WHERE company._id=companyToMajor.companyID AND company.name=? AND companyToMajor.majorID=major._id ORDER BY major.abbreviation;", nameArray);
		majorsCursor.moveToFirst();
		if(!majorsCursor.isAfterLast()) {
			do{
				String majorName = majorsCursor.getString(0);
				String majorAbbrev = majorsCursor.getString(1);
				majors.add(new Major(majorName, majorAbbrev));
			} while (majorsCursor.moveToNext());
		}
		majorsCursor.close();
		
		return majors;
	}
	
	/**
     * getPositionsForCompany - gets all the positions a specific company is hiring for
     * 
     * @param company - the name of the company to get the positions for
     * @param database - the name of the database to open (careerFairDB.db)
     * @return an ArrayList filled with positions
     */
	public static ArrayList<String> getPositionsForCompany(String company, SQLiteDatabase database) {
		String[] nameArray = {company};
		
		ArrayList<String> positions = new ArrayList<String>();
		Cursor positionsCursor = database.rawQuery("SELECT employmentType.type FROM company, companyToType, employmentType WHERE company._id=companyToType.companyID AND company.name=? AND companyToType.typeID=employmentType._id AND type<>'';", nameArray);
		positionsCursor.moveToFirst();
		if(!positionsCursor.isAfterLast()) {
			do{
				positions.add(positionsCursor.getString(0));
			} while (positionsCursor.moveToNext());
		}
		
		return positions;
	}
	
	/**
     * getWorkAuthsForCompany - gets all the work authorization types a specific company is looking for
     * 
     * @param company - the name of the company to get the work authorizations for
     * @param database - the name of the database to open (careerFairDB.db)
     * @return an ArrayList filled with work authorizations
     */
	public static ArrayList<String> getWorkAuthsForCompany(String company, SQLiteDatabase database) {
		String[] nameArray = {company};
		
		ArrayList<String> workAuths = new ArrayList<String>();
		Cursor workAuthsCursor = database.rawQuery("SELECT workAuth.type FROM company, companyToWorkAuth, workAuth WHERE company._id=companyToWorkAuth.companyID AND company.name=? AND companyToWorkAuth.workAuthID=workAuth._id AND type<>'';", nameArray);
		workAuthsCursor.moveToFirst();
		if(!workAuthsCursor.isAfterLast()) {
			do {
				workAuths.add(workAuthsCursor.getString(0));
			} while (workAuthsCursor.moveToNext());
		}
		
		return workAuths;
	}
}	

